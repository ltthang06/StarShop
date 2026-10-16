package vn.iotstar.starshop.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import vn.iotstar.starshop.entity.Order;
public record ManagerOrderView(Order order, List<Line> lines, String shipperName,
        String shipperEmail, LocalDateTime assignedAt) {
    public record Line(Long productId, String productName, int quantity,
            BigDecimal unitPrice, BigDecimal subtotal) {}
}

