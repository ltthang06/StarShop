package vn.iotstar.starshop.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.entity.Order;
import vn.iotstar.starshop.entity.OrderDetail;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.repository.OrderDetailRepository;
import vn.iotstar.starshop.repository.OrderRepository;
import vn.iotstar.starshop.service.OrderService;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Order> searchVendorOrders(
            Long shopId,
            Long ownerId,
            String keyword,
            OrderStatus status,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"));

        Specification<Order> specification =
                (root, query, cb) -> {

                    Predicate predicate =
                            cb.conjunction();

                    predicate = cb.and(
                            predicate,
                            cb.equal(
                                    root.get("shop").get("id"),
                                    shopId));

                    predicate = cb.and(
                            predicate,
                            cb.equal(
                                    root.get("shop")
                                            .get("owner")
                                            .get("id"),
                                    ownerId));

                    if (keyword != null
                            && !keyword.isBlank()) {

                        String search =
                                "%"
                                + keyword.trim()
                                        .toLowerCase()
                                + "%";

                        Predicate receiverName =
                                cb.like(
                                        cb.lower(
                                                root.get(
                                                        "receiverName")),
                                        search);

                        Predicate receiverPhone =
                                cb.like(
                                        cb.lower(
                                                root.get(
                                                        "receiverPhone")),
                                        search);

                        predicate = cb.and(
                                predicate,
                                cb.or(
                                        receiverName,
                                        receiverPhone));
                    }

                    if (status != null) {

                        predicate = cb.and(
                                predicate,
                                cb.equal(
                                        root.get("status"),
                                        status));
                    }

                    return predicate;
                };

        return orderRepository.findAll(
                specification,
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByOwner(
            Long orderId,
            Long shopId,
            Long ownerId) {

        return orderRepository
                .findByIdAndShopIdAndShopOwnerId(
                        orderId,
                        shopId,
                        ownerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy đơn hàng."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetail> getOrderDetails(
            Long orderId) {

        return orderDetailRepository
                .findByOrderIdOrderByIdAsc(
                        orderId);
    }

    @Override
    @Transactional
    public void confirmOrder(
            Long orderId,
            Long shopId,
            Long ownerId) {

        Order order = getOrderByOwner(
                orderId,
                shopId,
                ownerId);

        if (order.getStatus()
                != OrderStatus.NEW) {

            throw new IllegalArgumentException(
                    "Chỉ đơn hàng NEW mới có thể xác nhận.");
        }

        order.setStatus(
                OrderStatus.CONFIRMED);

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markReadyForPickup(
            Long orderId,
            Long shopId,
            Long ownerId) {

        Order order = getOrderByOwner(
                orderId,
                shopId,
                ownerId);

        if (order.getStatus()
                != OrderStatus.CONFIRMED) {

            throw new IllegalArgumentException(
                    "Chỉ đơn hàng CONFIRMED mới có thể chuyển sang READY_FOR_PICKUP.");
        }

        order.setStatus(
                OrderStatus.READY_FOR_PICKUP);

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(
            Long orderId,
            Long shopId,
            Long ownerId) {

        Order order = getOrderByOwner(
                orderId,
                shopId,
                ownerId);

        if (order.getStatus()
                != OrderStatus.NEW
                && order.getStatus()
                != OrderStatus.CONFIRMED) {

            throw new IllegalArgumentException(
                    "Chỉ đơn NEW hoặc CONFIRMED mới có thể hủy.");
        }

        order.setStatus(
                OrderStatus.CANCELLED);

        orderRepository.save(order);
    }
}