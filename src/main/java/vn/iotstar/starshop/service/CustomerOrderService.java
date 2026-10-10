package vn.iotstar.starshop.service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.repository.CustomerCatalogRepository;
import vn.iotstar.starshop.repository.CustomerOrderRepository;
import vn.iotstar.starshop.repository.OrderDetailRepository;
import vn.iotstar.starshop.repository.CustomerReviewRepository;
import vn.iotstar.starshop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserRepository userRepository;
    private final CustomerOrderRepository orderRepository;
    private final OrderDetailRepository detailRepository;
    private final CustomerCatalogRepository productRepository;
    private final CustomerReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public Page<CustomerOrderCard> history(String email, int page) {
        User user = currentUser(email);
        return orderRepository.findByUserId(
                user.getId(),
                PageRequest.of(Math.max(page, 0), 10,
                        Sort.by(Sort.Direction.DESC, "createdAt", "id"))
        ).map(this::toCard);
    }

    @Transactional
    public void cancel(String email, Long orderId) {
        User user = currentUser(email);
        Order order = ownedOrder(user.getId(), orderId);
        if (order.getStatus() != OrderStatus.NEW
                && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Đơn hàng không còn có thể hủy");
        }

        List<OrderDetail> details = detailRepository
                .findByOrderIdOrderByIdAsc(orderId);
        details.stream()
                .sorted(Comparator.comparing(detail ->
                        detail.getProduct().getId()))
                .forEach(detail -> restoreStock(detail));
        order.setStatus(OrderStatus.CANCELLED);
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
    }

    private CustomerOrderCard toCard(Order order) {
        List<CustomerOrderLine> lines = detailRepository
                .findByOrderIdOrderByIdAsc(order.getId())
                .stream()
                .map(detail -> new CustomerOrderLine(
                        detail.getId(),
                        detail.getProduct().getId(),
                        detail.getProduct().getName(),
                        detail.getQuantity(),
                        detail.getSubtotal(),
                        order.getStatus() == OrderStatus.DELIVERED
                                && !reviewRepository.existsByOrderDetailId(
                                        detail.getId())))
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
                order.getStatus() == OrderStatus.NEW
                        || order.getStatus() == OrderStatus.CONFIRMED,
                order.getStatus() == OrderStatus.DELIVERED
        );
    }

    private void restoreStock(OrderDetail detail) {
        Product product = productRepository.findByIdForUpdate(
                detail.getProduct().getId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Sản phẩm không còn tồn tại"));
        product.setQuantity(product.getQuantity() + detail.getQuantity());
        product.setSoldCount(Math.max(0,
                product.getSoldCount() - detail.getQuantity()));
        if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
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
