package vn.iotstar.starshop.service;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.dto.ManagerReport;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.repository.*;
@Service @RequiredArgsConstructor
public class ManagerReportService {
    private final ManagerUserRepository users;
    private final ManagerShopRepository shops;
    private final ProductRepository products;
    private final ManagerReportRepository orders;
    @Transactional(readOnly=true)
    public ManagerReport report(LocalDate from, LocalDate to) {
        LocalDate end=to == null ? LocalDate.now() : to;
        LocalDate start=from == null ? end.withDayOfMonth(1) : from;
        if (start.isAfter(end) || start.isBefore(LocalDate.of(2000,1,1)) || end.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Chọn khoảng ngày từ năm 2000 đến hôm nay, ngày đầu không sau ngày cuối");
        var status=new EnumMap<OrderStatus,Long>(OrderStatus.class);
        for (OrderStatus value: OrderStatus.values()) status.put(value,0L);
        var startAt=start.atStartOfDay(); var endAt=end.plusDays(1).atStartOfDay();
        for (Object[] row: orders.statusCounts(startAt,endAt)) status.put((OrderStatus)row[0],(Long)row[1]);
        BigDecimal revenue=orders.deliveredRevenue(startAt,endAt);
        return new ManagerReport(start,end,users.count(),shops.count(),products.count(),
            orders.countPeriod(startAt,endAt),revenue == null ? BigDecimal.ZERO : revenue,status);
    }
    public String csv(ManagerReport report) {
        StringBuilder csv=new StringBuilder("\uFEFFChi tieu,Gia tri\r\n");
        csv.append("Tu ngay,").append(report.from()).append("\r\nDen ngay,").append(report.to());
        csv.append("\r\nNguoi dung,").append(report.users()).append("\r\nShop,").append(report.shops());
        csv.append("\r\nSan pham,").append(report.products()).append("\r\nDon hang,").append(report.orders());
        csv.append("\r\nDoanh thu don da giao VND,").append(report.deliveredRevenue().toPlainString()).append("\r\n");
        report.byStatus().forEach((key,value) -> csv.append(key).append(',').append(value).append("\r\n"));
        return csv.toString();
    }
}
