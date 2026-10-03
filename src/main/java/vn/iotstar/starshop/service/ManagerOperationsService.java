package vn.iotstar.starshop.service;
import org.springframework.data.domain.Page;
import vn.iotstar.starshop.dto.ShippingProviderForm;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.OrderStatus;
public interface ManagerOperationsService {
    Page<User> users(String keyword, int page);
    void setUserLocked(Long id, boolean locked, String actorEmail, boolean actorAdmin);
    void saveProvider(ShippingProviderForm form);
    Page<Order> orders(OrderStatus status, int page);
    void assign(Long orderId, Long shipperId, Long providerId, String note);
}
