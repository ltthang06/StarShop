package vn.iotstar.starshop.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.PromotionRequest;
import vn.iotstar.starshop.entity.Promotion;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.enums.PromotionScope;
import vn.iotstar.starshop.enums.PromotionType;
import vn.iotstar.starshop.repository.PromotionRepository;
import vn.iotstar.starshop.service.PromotionService;
import vn.iotstar.starshop.service.ShopService;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl
        implements PromotionService {

    private static final ZoneId VIETNAM_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final PromotionRepository promotionRepository;
    private final ShopService shopService;

    @Override
    @Transactional(readOnly = true)
    public Page<Promotion> searchVendorPromotions(
            Long shopId,
            Long ownerId,
            String keyword,
            Boolean active,
            int page,
            int size) {

        shopService.getShopByOwner(
                shopId,
                ownerId
        );

        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(
                        Sort.Direction.DESC,
                        "startAt"
                )
        );

        return promotionRepository.searchVendorPromotions(
                shopId,
                PromotionScope.SHOP,
                keyword == null ? "" : keyword.trim(),
                active,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Promotion getPromotionByOwner(
            Long promotionId,
            Long shopId,
            Long ownerId) {

        shopService.getShopByOwner(
                shopId,
                ownerId
        );

        return promotionRepository
                .findByIdAndShop_IdAndScope(
                        promotionId,
                        shopId,
                        PromotionScope.SHOP
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy khuyến mãi."
                        )
                );
    }

    @Override
    @Transactional
    public Promotion createPromotion(
            Long shopId,
            Long ownerId,
            PromotionRequest request) {

        Shop shop = shopService.getShopByOwner(
                shopId,
                ownerId
        );

        validateRequest(
                request,
                null
        );

        LocalDateTime nowVietnam =
                getVietnamNow();

        LocalDateTime actualStartAt =
                resolveStartAt(
                        request.getStartAt(),
                        nowVietnam
                );

        validateFinalTime(
                actualStartAt,
                request.getEndAt()
        );

        Promotion promotion =
                new Promotion();

        promotion.setShop(shop);
        promotion.setScope(
                PromotionScope.SHOP
        );

        applyRequest(
                promotion,
                request
        );

        promotion.setStartAt(
                actualStartAt
        );

        promotion.setEndAt(
                request.getEndAt()
        );

        return promotionRepository.save(
                promotion
        );
    }

    @Override
    @Transactional
    public Promotion updatePromotion(
            Long promotionId,
            Long shopId,
            Long ownerId,
            PromotionRequest request) {

        Promotion promotion =
                getPromotionByOwner(
                        promotionId,
                        shopId,
                        ownerId
                );

        validateRequest(
                request,
                promotionId
        );

        LocalDateTime nowVietnam =
                getVietnamNow();

        LocalDateTime actualStartAt;

        if (promotion.getStartAt() != null
                && !promotion.getStartAt()
                .isAfter(nowVietnam)) {

            actualStartAt =
                    promotion.getStartAt();

        } else {

            actualStartAt =
                    resolveStartAt(
                            request.getStartAt(),
                            nowVietnam
                    );
        }

        validateFinalTime(
                actualStartAt,
                request.getEndAt()
        );

        applyRequest(
                promotion,
                request
        );

        promotion.setStartAt(
                actualStartAt
        );

        promotion.setEndAt(
                request.getEndAt()
        );

        return promotionRepository.save(
                promotion
        );
    }

    @Override
    @Transactional
    public void setActive(
            Long promotionId,
            Long shopId,
            Long ownerId,
            boolean active) {

        Promotion promotion =
                getPromotionByOwner(
                        promotionId,
                        shopId,
                        ownerId
                );

        promotion.setActive(active);

        promotionRepository.save(
                promotion
        );
    }

    private void validateRequest(
            PromotionRequest request,
            Long currentId) {

        String code =
                request.getCode()
                        .trim()
                        .toUpperCase();

        promotionRepository
                .findByCodeIgnoreCase(code)
                .ifPresent(existing -> {

                    if (currentId == null
                            || !existing.getId()
                            .equals(currentId)) {

                        throw new IllegalArgumentException(
                                "Mã khuyến mãi đã tồn tại."
                        );
                    }
                });

        if (request.getStartAt() == null
                || request.getEndAt() == null) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập đầy đủ thời gian bắt đầu và kết thúc."
            );
        }

        if (!request.getStartAt()
                .isBefore(
                        request.getEndAt()
                )) {

            throw new IllegalArgumentException(
                    "Thời gian kết thúc phải sau thời gian bắt đầu."
            );
        }

        if (request.getQuantity() == null
                || request.getQuantity() < 0) {

            throw new IllegalArgumentException(
                    "Số lượng khuyến mãi không hợp lệ."
            );
        }

        PromotionType type =
                request.getType();

        if (type == null) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn loại khuyến mãi."
            );
        }

        if (type == PromotionType.PERCENT) {

            BigDecimal value =
                    request.getDiscountValue();

            if (value == null
                    || value.compareTo(
                    BigDecimal.ZERO
            ) <= 0
                    || value.compareTo(
                    BigDecimal.valueOf(100)
            ) > 0) {

                throw new IllegalArgumentException(
                        "Giảm theo phần trăm phải lớn hơn 0 và không vượt quá 100."
                );
            }

        } else if (type
                == PromotionType.FIXED_AMOUNT) {

            BigDecimal value =
                    request.getDiscountValue();

            if (value == null
                    || value.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

                throw new IllegalArgumentException(
                        "Số tiền giảm phải lớn hơn 0."
                );
            }
        }

        validateNonNegative(
                request.getMinOrderAmount(),
                "Giá trị đơn tối thiểu"
        );

        validateNonNegative(
                request.getMaxDiscountAmount(),
                "Mức giảm tối đa"
        );
    }

    private void validateFinalTime(
            LocalDateTime startAt,
            LocalDateTime endAt) {

        if (startAt == null
                || endAt == null
                || !startAt.isBefore(endAt)) {

            throw new IllegalArgumentException(
                    "Thời gian kết thúc phải sau thời gian bắt đầu."
            );
        }

        LocalDateTime nowVietnam =
                getVietnamNow();

        if (!endAt.isAfter(nowVietnam)) {

            throw new IllegalArgumentException(
                    "Thời gian kết thúc phải nằm trong tương lai."
            );
        }
    }

    private LocalDateTime resolveStartAt(
            LocalDateTime requestedStartAt,
            LocalDateTime nowVietnam) {

        if (requestedStartAt == null) {
            return nowVietnam;
        }

        if (requestedStartAt
                .isAfter(nowVietnam)) {

            return requestedStartAt;
        }

        return nowVietnam;
    }

    private LocalDateTime getVietnamNow() {

        return LocalDateTime.now(
                VIETNAM_ZONE
        ).withNano(0);
    }

    private void validateNonNegative(
            BigDecimal value,
            String fieldName) {

        if (value != null
                && value.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new IllegalArgumentException(
                    fieldName
                            + " phải lớn hơn hoặc bằng 0."
            );
        }
    }

    private void applyRequest(
            Promotion promotion,
            PromotionRequest request) {

        promotion.setCode(
                request.getCode()
                        .trim()
                        .toUpperCase()
        );

        promotion.setName(
                request.getName()
                        .trim()
        );

        promotion.setDescription(
                request.getDescription() == null
                        ? null
                        : request.getDescription()
                        .trim()
        );

        promotion.setType(
                request.getType()
        );

        if (request.getType()
                == PromotionType.FREE_SHIPPING) {

            promotion.setDiscountValue(
                    BigDecimal.ZERO
            );

        } else {

            promotion.setDiscountValue(
                    request.getDiscountValue()
            );
        }

        promotion.setMinOrderAmount(
                request.getMinOrderAmount() == null
                        ? BigDecimal.ZERO
                        : request.getMinOrderAmount()
        );

        promotion.setMaxDiscountAmount(
                request.getMaxDiscountAmount()
        );

        promotion.setQuantity(
                request.getQuantity()
        );

        promotion.setActive(
                request.isActive()
        );

        promotion.setScope(
                PromotionScope.SHOP
        );
    }
}