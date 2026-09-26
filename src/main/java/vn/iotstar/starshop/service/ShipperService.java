package vn.iotstar.starshop.service;

import org.springframework.data.domain.Page;

import vn.iotstar.starshop.entity.ShipperAssignment;
import vn.iotstar.starshop.enums.OrderStatus;

public interface ShipperService {

    Page<ShipperAssignment> getAssignments(
            Long shipperId,
            int page,
            int size
    );

    ShipperAssignment getAssignment(
            Long assignmentId,
            Long shipperId
    );

    void updateOrderStatus(
            Long assignmentId,
            Long shipperId,
            OrderStatus newStatus
    );

    long countAll(Long shipperId);

    long countByStatus(
            Long shipperId,
            OrderStatus status
    );
}