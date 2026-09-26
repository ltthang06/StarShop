package vn.iotstar.starshop.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.iotstar.starshop.entity.Order;

@Repository
public interface VendorStatisticRepository
        extends JpaRepository<Order, Long> {

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM products
                    WHERE shop_id = :shopId
                    """,
            nativeQuery = true
    )
    long countProducts(
            @Param("shopId") Long shopId
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM products
                    WHERE shop_id = :shopId
                      AND status = 'ACTIVE'
                    """,
            nativeQuery = true
    )
    long countActiveProducts(
            @Param("shopId") Long shopId
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM products
                    WHERE shop_id = :shopId
                      AND (
                            quantity <= 0
                            OR status = 'OUT_OF_STOCK'
                          )
                    """,
            nativeQuery = true
    )
    long countOutOfStockProducts(
            @Param("shopId") Long shopId
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM orders
                    WHERE shop_id = :shopId
                    """,
            nativeQuery = true
    )
    long countOrders(
            @Param("shopId") Long shopId
    );

    @Query(
            value = """
                    SELECT
                        status,
                        COUNT(*)
                    FROM orders
                    WHERE shop_id = :shopId
                    GROUP BY status
                    """,
            nativeQuery = true
    )
    List<Object[]> countOrdersByStatus(
            @Param("shopId") Long shopId
    );

    @Query(
            value = """
                    SELECT COALESCE(SUM(total_amount), 0)
                    FROM orders
                    WHERE shop_id = :shopId
                      AND status = 'DELIVERED'
                      AND created_at >= :fromDate
                      AND created_at < :toDate
                    """,
            nativeQuery = true
    )
    Object sumRevenue(
            @Param("shopId") Long shopId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM orders
                    WHERE shop_id = :shopId
                      AND status = 'DELIVERED'
                      AND created_at >= :fromDate
                      AND created_at < :toDate
                    """,
            nativeQuery = true
    )
    long countDeliveredOrders(
            @Param("shopId") Long shopId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query(
            value = """
                    SELECT COALESCE(SUM(od.quantity), 0)
                    FROM order_details od
                    JOIN orders o
                      ON o.id = od.order_id
                    WHERE o.shop_id = :shopId
                      AND o.status = 'DELIVERED'
                    """,
            nativeQuery = true
    )
    Object sumAllSoldQuantity(
            @Param("shopId") Long shopId
    );

    @Query(
            value = """
                    SELECT COALESCE(SUM(od.quantity), 0)
                    FROM order_details od
                    JOIN orders o
                      ON o.id = od.order_id
                    WHERE o.shop_id = :shopId
                      AND o.status = 'DELIVERED'
                      AND o.created_at >= :fromDate
                      AND o.created_at < :toDate
                    """,
            nativeQuery = true
    )
    Object sumSoldQuantity(
            @Param("shopId") Long shopId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query(
            value = """
                    SELECT TOP 5
                        p.id,
                        p.name,
                        SUM(od.quantity) AS quantity_sold,
                        SUM(od.subtotal) AS revenue
                    FROM order_details od
                    JOIN orders o
                      ON o.id = od.order_id
                    JOIN products p
                      ON p.id = od.product_id
                    WHERE o.shop_id = :shopId
                      AND o.status = 'DELIVERED'
                    GROUP BY
                        p.id,
                        p.name
                    ORDER BY
                        SUM(od.quantity) DESC,
                        p.id ASC
                    """,
            nativeQuery = true
    )
    List<Object[]> findTopProducts(
            @Param("shopId") Long shopId
    );

    @Query(
            value = """
                    SELECT
                        CAST(created_at AS date) AS revenue_date,
                        SUM(total_amount) AS revenue
                    FROM orders
                    WHERE shop_id = :shopId
                      AND status = 'DELIVERED'
                      AND created_at >= :fromDate
                      AND created_at < :toDate
                    GROUP BY CAST(created_at AS date)
                    ORDER BY revenue_date ASC
                    """,
            nativeQuery = true
    )
    List<Object[]> findDailyRevenue(
            @Param("shopId") Long shopId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}