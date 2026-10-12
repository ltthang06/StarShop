package vn.iotstar.starshop.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import vn.iotstar.starshop.enums.OrderStatus;
public record ManagerReport(LocalDate from, LocalDate to, long users, long shops, long products,
    long orders, BigDecimal deliveredRevenue, Map<OrderStatus,Long> byStatus) {}
