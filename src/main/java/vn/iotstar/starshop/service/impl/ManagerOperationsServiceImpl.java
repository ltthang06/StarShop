package vn.iotstar.starshop.service.impl;
import java.util.Locale;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.ShippingProviderForm;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.ManagerOperationsService;
@Service @RequiredArgsConstructor
public class ManagerOperationsServiceImpl implements ManagerOperationsService {
    private final ManagerUserRepository users;
    private final ShippingProviderRepository providers;
    private final ManagerOrderRepository orders;
    private final ShipperAssignmentRepository assignments;

    @Override @Transactional(readOnly=true)
    public Page<User> users(String keyword, int page) {
        String pattern = "%" + (keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT)) + "%";
        Specification<User> spec = (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get("fullName")), pattern), cb.like(cb.lower(root.get("email")), pattern));
        return users.findAll(spec, PageRequest.of(Math.max(0,page), 10, Sort.by("id").descending()));
    }
    @Override @Transactional
    public void setUserLocked(Long id, boolean locked, String actorEmail, boolean actorAdmin) {
        User target = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        if (target.getEmail().equalsIgnoreCase(actorEmail))
            throw new IllegalArgumentException("Không thể khóa hoặc mở khóa tài khoản của chính bạn");
        boolean protectedRole = target.getRoles().stream().anyMatch(r ->
            r.getName() == RoleName.ADMIN || r.getName() == RoleName.MANAGER);
        if (protectedRole && !actorAdmin)
            throw new IllegalArgumentException("Chỉ Admin được quản lý tài khoản quản trị");
        if (target.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN))
            throw new IllegalArgumentException("Chức năng này không khóa tài khoản Admin");
        if (locked && target.getStatus() != UserStatus.ACTIVE)
            throw new IllegalArgumentException("Chỉ khóa tài khoản đang hoạt động");
        if (!locked && target.getStatus() != UserStatus.LOCKED)
            throw new IllegalArgumentException("Chỉ mở khóa tài khoản đã bị khóa");
        target.setStatus(locked ? UserStatus.LOCKED : UserStatus.ACTIVE);
        users.save(target);
    }
    @Override @Transactional
    public void saveProvider(ShippingProviderForm form) {
        String name = form.getName().trim();
        boolean duplicate = form.getId() == null ? providers.existsByNameIgnoreCase(name)
            : providers.existsByNameIgnoreCaseAndIdNot(name, form.getId());
        if (duplicate) throw new IllegalArgumentException("Tên đơn vị vận chuyển đã tồn tại");
        ShippingProvider provider = form.getId() == null ? new ShippingProvider()
            : providers.findById(form.getId()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển"));
        provider.setName(name); provider.setPhone(form.getPhone()); provider.setBaseFee(form.getBaseFee());
        provider.setEstimatedDays(form.getEstimatedDays()); provider.setActive(form.isActive());
        providers.save(provider);
    }
    @Override @Transactional(readOnly=true)
    public Page<Order> orders(OrderStatus status, int page) {
        Specification<Order> spec = (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"),status);
        return orders.findAll(spec, PageRequest.of(Math.max(0,page),10,Sort.by("createdAt").descending()));
    }
    @Override @Transactional
    public void assign(Long orderId, Long shipperId, Long providerId, String note) {
        Order order = orders.findForAssignment(orderId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP || assignments.findByOrder_Id(orderId).isPresent())
            throw new IllegalArgumentException("Chỉ phân công đơn sẵn sàng lấy hàng và chưa có shipper");
        User shipper = users.findById(shipperId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy shipper"));
        if (shipper.getStatus() != UserStatus.ACTIVE || shipper.getRoles().stream().noneMatch(r -> r.getName() == RoleName.SHIPPER))
            throw new IllegalArgumentException("Shipper phải hoạt động và có quyền SHIPPER");
        ShippingProvider provider = providers.findById(providerId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển"));
        if (!provider.isActive()) throw new IllegalArgumentException("Đơn vị vận chuyển đã tắt");
        if (note != null && note.length() > 500) throw new IllegalArgumentException("Ghi chú tối đa 500 ký tự");
        ShipperAssignment assignment = new ShipperAssignment();
        assignment.setOrder(order); assignment.setShipper(shipper); assignment.setNote(note);
        assignments.save(assignment);
        order.setShippingProvider(provider);
        order.setStatus(OrderStatus.ASSIGNED);
        orders.save(order);
    }
}
