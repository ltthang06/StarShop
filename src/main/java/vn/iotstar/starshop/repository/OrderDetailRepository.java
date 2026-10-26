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

    @EntityGraph(attributePaths = "product")
    List<OrderDetail> findByOrderIdInOrderByIdAsc(java.util.Collection<Long> orderIds);

    interface InventoryLine {
        Long getProductId();
        int getQuantity();
    }

    @org.springframework.data.jpa.repository.Query("select d.product.id as productId, d.quantity as quantity from OrderDetail d where d.order.id = :orderId order by d.product.id, d.id")
    List<InventoryLine> inventoryLines(@org.springframework.data.repository.query.Param("orderId") Long orderId);
}
