package vn.iotstar.starshop.repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import vn.iotstar.starshop.entity.Order;
public interface ManagerReportRepository extends JpaRepository<Order,Long> {
    @Query("select count(distinct o.user.id) from Order o where o.createdAt >= :start and o.createdAt < :end and lower(o.shop.manager.email)=lower(:managerEmail)")
    long customersInPeriod(LocalDateTime start, LocalDateTime end, String managerEmail);
    @Query("select count(o) from Order o where o.createdAt >= :start and o.createdAt < :end and (:managerEmail is null or exists (select s.id from Shop s where s.id=o.shop.id and lower(s.manager.email)=lower(:managerEmail)))")
    long countPeriod(LocalDateTime start, LocalDateTime end, String managerEmail);
    @Query("select sum(o.totalAmount) from Order o where o.status = vn.iotstar.starshop.enums.OrderStatus.DELIVERED and o.createdAt >= :start and o.createdAt < :end and (:managerEmail is null or exists (select s.id from Shop s where s.id=o.shop.id and lower(s.manager.email)=lower(:managerEmail)))")
    BigDecimal deliveredRevenue(LocalDateTime start, LocalDateTime end, String managerEmail);
    @Query("select o.status, count(o) from Order o where o.createdAt >= :start and o.createdAt < :end and (:managerEmail is null or exists (select s.id from Shop s where s.id=o.shop.id and lower(s.manager.email)=lower(:managerEmail))) group by o.status")
    List<Object[]> statusCounts(LocalDateTime start, LocalDateTime end, String managerEmail);
}
