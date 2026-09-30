package vn.iotstar.starshop.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.entity.Order;
import vn.iotstar.starshop.entity.ShipperAssignment;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.repository.OrderRepository;
import vn.iotstar.starshop.repository.ShipperAssignmentRepository;
import vn.iotstar.starshop.service.ShipperService;

@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService {

    private final ShipperAssignmentRepository shipperAssignmentRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ShipperAssignment> getAssignments(
            Long shipperId,
            int page,
            int size) {

        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(
                        Sort.Direction.DESC,
                        "assignedAt"
                )
        );

        return shipperAssignmentRepository.findByShipper_Id(
                shipperId,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ShipperAssignment getAssignment(
            Long assignmentId,
            Long shipperId) {

        return shipperAssignmentRepository
                .findByIdAndShipper_Id(
                        assignmentId,
                        shipperId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy đơn giao hàng."
                        )
                );
    }

    @Override
    @Transactional
    public void updateOrderStatus(
            Long assignmentId,
            Long shipperId,
            OrderStatus newStatus) {

        ShipperAssignment assignment = getAssignment(
                assignmentId,
                shipperId
        );

        Order order = assignment.getOrder();

        OrderStatus currentStatus = order.getStatus();

        if (!isValidTransition(
                currentStatus,
                newStatus
        )) {
            throw new IllegalArgumentException(
                    "Không thể chuyển trạng thái từ "
                            + currentStatus
                            + " sang "
                            + newStatus
            );
        }

        order.setStatus(newStatus);

        if (newStatus == OrderStatus.PICKED_UP) {
            assignment.setPickedUpAt(
                    LocalDateTime.now()
            );
        }

        if (newStatus == OrderStatus.DELIVERED) {
            assignment.setDeliveredAt(
                    LocalDateTime.now()
            );
        }

        orderRepository.save(order);
        shipperAssignmentRepository.save(assignment);
    }

    private boolean isValidTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus) {

        if (currentStatus == OrderStatus.ASSIGNED) {
            return newStatus == OrderStatus.PICKED_UP;
        }

        if (currentStatus == OrderStatus.PICKED_UP) {
            return newStatus == OrderStatus.SHIPPING;
        }

        if (currentStatus == OrderStatus.SHIPPING) {
            return newStatus == OrderStatus.DELIVERED
                    || newStatus
                    == OrderStatus.DELIVERY_FAILED;
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll(Long shipperId) {

        return shipperAssignmentRepository
                .countByShipper_Id(shipperId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(
            Long shipperId,
            OrderStatus status) {

        return shipperAssignmentRepository
                .countByShipper_IdAndOrder_Status(
                        shipperId,
                        status
                );
    }
}