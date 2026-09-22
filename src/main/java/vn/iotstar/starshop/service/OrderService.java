package vn.iotstar.starshop.service;

import java.util.List;

import org.springframework.data.domain.Page;

import vn.iotstar.starshop.entity.Order;
import vn.iotstar.starshop.entity.OrderDetail;
import vn.iotstar.starshop.enums.OrderStatus;

public interface OrderService {

    Page<Order> searchVendorOrders(
            Long shopId,
            Long ownerId,
            String keyword,
            OrderStatus status,
            int page,
            int size);

    Order getOrderByOwner(
            Long orderId,
            Long shopId,
            Long ownerId);

    List<OrderDetail> getOrderDetails(Long orderId);

    void confirmOrder(
            Long orderId,
            Long shopId,
            Long ownerId);

    void markReadyForPickup(
            Long orderId,
            Long shopId,
            Long ownerId);

    void cancelOrder(
            Long orderId,
            Long shopId,
            Long ownerId);
}