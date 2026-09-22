package vn.iotstar.starshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.iotstar.starshop.entity.Order;

@Repository
public interface OrderRepository
        extends JpaRepository<Order, Long>,
                JpaSpecificationExecutor<Order> {

    Optional<Order> findByIdAndShopIdAndShopOwnerId(
            Long orderId,
            Long shopId,
            Long ownerId);
}