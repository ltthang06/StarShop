package vn.iotstar.starshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.iotstar.starshop.entity.OrderDetail;

@Repository
public interface OrderDetailRepository
        extends JpaRepository<OrderDetail, Long> {

    @EntityGraph(attributePaths = "product")
    List<OrderDetail> findByOrderIdOrderByIdAsc(Long orderId);
}