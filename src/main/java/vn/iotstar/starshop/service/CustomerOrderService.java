package vn.iotstar.starshop.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.CustomerOrderCard;
import vn.iotstar.starshop.dto.CustomerOrderLine;
import vn.iotstar.starshop.entity.Order;
import vn.iotstar.starshop.entity.OrderDetail;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.repository.CustomerOrderRepository;
import vn.iotstar.starshop.repository.OrderDetailRepository;
import vn.iotstar.starshop.repository.CustomerReviewRepository;
import vn.iotstar.starshop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {
    private final vn.iotstar.starshop.service.NotificationService notifications;


    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserRepository userRepository;
    private final CustomerOrderRepository orderRepository;
    private final OrderDetailRepository detailRepository;
    private final OrderInventoryService inventory;
    private final CustomerReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public Page<CustomerOrderCard> history(String email, int page) {
        return history(email, null, page);
    }

    @Transactional(readOnly = true)
    public Page<CustomerOrderCard> history(String email, OrderStatus status, int page) {
        User user = currentUser(email);
        var pageable = PageRequest.of(Math.max(page, 0), 10,
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        Page<Order> result = status == null ? orderRepository.findByUserId(user.getId(), pageable)
                : orderRepository.findByUserIdAndStatus(user.getId(), status, pageable);
        if (result.isEmpty()) return result.map(order -> toCard(order, List.of(), java.util.Set.of()));
        var details = detailRepository.findByOrderIdInOrderByIdAsc(result.stream().map(Order::getId).toList());
        var byOrder = details.stream().collect(java.util.stream.Collectors.groupingBy(d -> d.getOrder().getId()));
        var eligibleIds = details.stream().filter(d -> d.getOrder().getStatus() == OrderStatus.DELIVERED)
                .map(OrderDetail::getId).toList();
        java.util.Set<Long> reviewed = eligibleIds.isEmpty() ? java.util.Set.of()
                : new java.util.HashSet<>(reviewRepository.reviewedDetailIds(eligibleIds));
        return result.map(order -> toCard(order, byOrder.getOrDefault(order.getId(), List.of()), reviewed));
    }

    @Transactional
    public void cancel(String email, Long orderId) {
        User user = currentUser(email);
        Order order = ownedOrder(user.getId(), orderId);
        if(order.getPaymentMethod()==vn.iotstar.starshop.enums.PaymentMethod.VNPAY)
            throw new IllegalArgumentException("Đơn VNPAY cần được đối soát thanh toán. Đơn thanh toán thất bại sẽ tự hủy khi nhận kết quả từ VNPAY");
        if (order.getStatus() != OrderStatus.NEW
                && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Đơn hàng không còn có thể hủy");
        }

        inventory.restore(orderId);
        order.setStatus(OrderStatus.CANCELLED);
        notifications.orderChanged(order);
    }

    @Transactional
    public void requestReturn(String email, Long orderId) {
        User user = currentUser(email);
        Order order = ownedOrder(user.getId(), orderId);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException(
                    "Chỉ có thể yêu cầu trả đơn đã giao");
        }
        order.setStatus(OrderStatus.RETURN_REQUESTED);
        notifications.orderChanged(order);
    }

    private CustomerOrderCard toCard(Order order, List<OrderDetail> details, java.util.Set<Long> reviewed) {
        List<CustomerOrderLine> lines = details.stream()
                .map(detail -> new CustomerOrderLine(
                        detail.getId(),
                        detail.getProduct().getId(),
                        detail.getProduct().getName(),
                        detail.getQuantity(),
                        detail.getSubtotal(),
                        order.getStatus() == OrderStatus.DELIVERED
                                && !reviewed.contains(detail.getId())))
                .toList();

        return new CustomerOrderCard(
                order.getId(),
                order.getShop().getName(),
                order.getCreatedAt().format(DATE_FORMAT),
                order.getStatus(),
                order.getShippingAddress(),
                order.getShippingFee(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                lines,
                order.getPaymentMethod()!=vn.iotstar.starshop.enums.PaymentMethod.VNPAY
                        && (order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.CONFIRMED),
                order.getStatus() == OrderStatus.DELIVERED,
                order.getPaymentMethod(),order.getPaymentStatus()
        );
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tài khoản"));
    }

    private Order ownedOrder(Long userId, Long orderId) {
        return orderRepository.findOwnedForUpdate(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy đơn hàng"));
    }
}
