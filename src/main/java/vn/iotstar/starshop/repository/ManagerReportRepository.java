package vn.iotstar.starshop.repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import vn.iotstar.starshop.entity.Order;
public interface ManagerReportRepository extends JpaRepository<Order,Long> {
    @Query("select count(o) from Order o where o.createdAt >= :start and o.createdAt < :end")
    long countPeriod(LocalDateTime start, LocalDateTime end);
    @Query("select sum(o.totalAmount) from Order o where o.status = vn.iotstar.starshop.enums.OrderStatus.DELIVERED and o.createdAt >= :start and o.createdAt < :end")
    BigDecimal deliveredRevenue(LocalDateTime start, LocalDateTime end);
    @Query("select o.status, count(o) from Order o where o.createdAt >= :start and o.createdAt < :end group by o.status")
    List<Object[]> statusCounts(LocalDateTime start, LocalDateTime end);
}
