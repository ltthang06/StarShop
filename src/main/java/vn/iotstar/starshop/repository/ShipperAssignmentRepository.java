package vn.iotstar.starshop.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.iotstar.starshop.entity.ShipperAssignment;
import vn.iotstar.starshop.enums.OrderStatus;

@Repository
public interface ShipperAssignmentRepository
        extends JpaRepository<ShipperAssignment, Long> {

    Page<ShipperAssignment> findByShipper_Id(
            Long shipperId,
            Pageable pageable
    );

    Optional<ShipperAssignment> findByIdAndShipper_Id(
            Long assignmentId,
            Long shipperId
    );

    Optional<ShipperAssignment> findByOrder_Id(Long orderId);

    long countByShipper_Id(Long shipperId);

    long countByShipper_IdAndOrder_Status(
            Long shipperId,
            OrderStatus status
    );
}