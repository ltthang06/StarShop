package vn.iotstar.starshop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.dto.PromotionRequest;
import vn.iotstar.starshop.repository.*;

@Service @RequiredArgsConstructor
public class ManagerCatalogService {
    private final ProductRepository products;
    private final ProductModerationRepository moderations;
    private final PromotionRepository promotionCodes;
    private final SystemPromotionRepository promotions;

    @Transactional(readOnly=true)
    public Page<Product> products(String keyword, Long shopId, ProductStatus status, int page) {
        String pattern = "%" + (keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT)) + "%";
        Specification<Product> spec = (root, query, cb) -> {
            var predicate = cb.like(cb.lower(root.get("name")), pattern);
            if (shopId != null) predicate = cb.and(predicate, cb.equal(root.get("shop").get("id"), shopId));
            if (status != null) predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            return predicate;
        };
        return products.findAll(spec, PageRequest.of(Math.max(0,page), 10, Sort.by("id").descending()));
    }

    @Transactional
    public void moderate(Long id, boolean blocked, String reason, String actor) {
        Product product = products.findForModeration(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        String text = reason == null ? "" : reason.trim();
        if (text.length() > 500 || blocked && text.length() < 10)
            throw new IllegalArgumentException("Lý do khóa cần từ 10 đến 500 ký tự");
        ProductModeration record = moderations.findById(id).orElseGet(ProductModeration::new);
        if (!blocked && !record.isBlocked())
            throw new IllegalArgumentException("Sản phẩm chưa bị quản trị khóa");
        record.setProductId(id); record.setBlocked(blocked); record.setReason(text);
        record.setModeratorEmail(actor); record.setUpdatedAt(LocalDateTime.now());
        moderations.save(record);
        // Mở kiểm duyệt giữ sản phẩm ẩn; Vendor tự kiểm tra nội dung rồi bật lại.
        product.setStatus(ProductStatus.INACTIVE);
        products.save(product);
    }

    @Transactional(readOnly=true)
    public Product product(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
    }

    @Transactional(readOnly=true)
    public Page<Promotion> promotions(String keyword, int page) {
        String pattern = "%" + (keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT)) + "%";
        Specification<Promotion> spec = (root, query, cb) -> cb.and(
            cb.equal(root.get("scope"), PromotionScope.SYSTEM),
            cb.or(cb.like(cb.lower(root.get("code")),pattern), cb.like(cb.lower(root.get("name")),pattern)));
        return promotions.findAll(spec, PageRequest.of(Math.max(0,page),10,Sort.by("id").descending()));
    }

    @Transactional(readOnly=true)
    public Promotion systemPromotion(Long id) {
        Promotion p = promotions.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
        requireSystem(p); return p;
    }

    @Transactional
    public Promotion savePromotion(Long id, PromotionRequest form) {
        if (form.getCode() == null || !form.getCode().trim().matches("[A-Za-z0-9_-]{3,50}"))
            throw new IllegalArgumentException("Mã gồm 3–50 chữ, số, dấu gạch ngang hoặc gạch dưới");
        if (form.getName() == null || form.getName().isBlank() || form.getName().length() > 150
            || form.getDescription() != null && form.getDescription().length() > 500)
            throw new IllegalArgumentException("Tên hoặc mô tả không hợp lệ");
        if (form.getStartAt() == null || form.getEndAt() == null || !form.getStartAt().isBefore(form.getEndAt()))
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        if (form.getType() == null || form.getQuantity() == null || form.getQuantity() < 0)
            throw new IllegalArgumentException("Loại hoặc số lượng không hợp lệ");
        validateMoney(form.getDiscountValue()); validateMoney(form.getMinOrderAmount()); validateMoney(form.getMaxDiscountAmount());
        BigDecimal value = form.getType() == PromotionType.FREE_SHIPPING ? BigDecimal.ZERO : form.getDiscountValue();
        if (value == null || form.getType() != PromotionType.FREE_SHIPPING && value.signum() <= 0
            || form.getType() == PromotionType.PERCENT && value.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("Mức giảm phải dương; phần trăm tối đa 100");
        String code = form.getCode().trim().toUpperCase(Locale.ROOT);
        promotionCodes.findByCodeIgnoreCase(code).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw new IllegalArgumentException("Mã đã tồn tại");
        });
        Promotion p = id == null ? new Promotion() : promotions.findLocked(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
        if (id != null) requireSystem(p);
        p.setScope(PromotionScope.SYSTEM); p.setShop(null); p.setCode(code); p.setName(form.getName().trim());
        p.setDescription(form.getDescription()); p.setType(form.getType()); p.setDiscountValue(value);
        p.setMinOrderAmount(form.getMinOrderAmount()); p.setMaxDiscountAmount(form.getMaxDiscountAmount());
        p.setQuantity(form.getQuantity()); p.setStartAt(form.getStartAt()); p.setEndAt(form.getEndAt()); p.setActive(form.isActive());
        return promotions.save(p);
    }

    @Transactional
    public void togglePromotion(Long id, boolean active) {
        Promotion p = promotions.findLocked(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
        requireSystem(p); p.setActive(active);
    }

    private void requireSystem(Promotion p) {
        if (p.getScope() != PromotionScope.SYSTEM) throw new IllegalArgumentException("Chỉ quản lý voucher SYSTEM tại đây");
    }
    private void validateMoney(BigDecimal value) {
        if (value != null && (value.signum() < 0 || value.scale() > 2 || value.precision() - value.scale() > 13))
            throw new IllegalArgumentException("Số tiền không âm, tối đa 13 số nguyên và 2 số thập phân");
    }
}
