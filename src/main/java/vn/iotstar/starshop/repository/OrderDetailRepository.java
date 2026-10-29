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

    @org.springframework.data.jpa.repository.Query("select d from OrderDetail d join fetch d.product p join fetch d.order o "
            + "where p.id=:productId and lower(o.user.email)=lower(:email) "
            + "and o.status=vn.iotstar.starshop.enums.OrderStatus.DELIVERED "
            + "and not exists (select r.id from Review r where r.orderDetail.id=d.id) order by o.id desc,d.id desc")
    List<OrderDetail> findReviewable(@org.springframework.data.repository.query.Param("productId") Long productId,
            @org.springframework.data.repository.query.Param("email") String email,org.springframework.data.domain.Pageable pageable);
}
