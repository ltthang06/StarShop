package vn.iotstar.starshop.config;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/** Chỉ định dạng hiển thị; giữ nguyên giá trị enum và số tiền trong database. */
@Component
public class ViewFormat {
    public String money(Number value) {
        if (value == null) return "—";
        var symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.##", symbols).format(value) + " ₫";
    }

    public String label(Enum<?> value) {
        if (value == null) return "—";
        if (value == vn.iotstar.starshop.enums.UserStatus.INACTIVE) return "Chưa kích hoạt";
        if (value == vn.iotstar.starshop.enums.ProductStatus.INACTIVE) return "Ngừng bán";
        if (value == vn.iotstar.starshop.enums.ProductStatus.ACTIVE) return "Đang bán";
        if (value == vn.iotstar.starshop.enums.ShopStatus.PENDING) return "Chờ duyệt";
        if (value == vn.iotstar.starshop.enums.PaymentStatus.PENDING) return "Chờ thanh toán";
        return switch (value.name()) {
            case "NEW" -> "Đơn mới";
            case "CONFIRMED" -> "Đã xác nhận";
            case "READY_FOR_PICKUP" -> "Chờ lấy hàng";
            case "ASSIGNED" -> "Đã phân công";
            case "PICKED_UP" -> "Đã lấy hàng";
            case "SHIPPING" -> "Đang giao";
            case "DELIVERED" -> "Đã giao";
            case "CANCELLED" -> "Đã hủy";
            case "RETURN_REQUESTED" -> "Yêu cầu trả hàng";
            case "RETURNED" -> "Đã nhận hàng trả";
            case "REFUNDED" -> "Đã hoàn tiền";
            case "DELIVERY_FAILED" -> "Giao không thành công";
            case "ACTIVE" -> "Hoạt động";
            case "INACTIVE" -> "Chưa kích hoạt / đã tắt";
            case "LOCKED", "BLOCKED" -> "Đã khóa";
            case "PENDING" -> "Đang chờ";
            case "REJECTED" -> "Đã từ chối";
            case "OUT_OF_STOCK" -> "Hết hàng";
            case "PAID" -> "Đã thanh toán";
            case "FAILED" -> "Không thành công";
            case "COD" -> "Thanh toán khi nhận hàng";
            case "VNPAY" -> "VNPAY";
            case "PERCENT" -> "Giảm phần trăm";
            case "FIXED_AMOUNT" -> "Giảm số tiền";
            case "FREE_SHIPPING" -> "Miễn phí vận chuyển";
            case "SYSTEM" -> "Toàn hệ thống";
            case "SHOP" -> "Cửa hàng";
            case "USER" -> "Khách hàng";
            case "VENDOR" -> "Người bán";
            case "MANAGER" -> "Quản lý";
            case "ADMIN" -> "Quản trị viên";
            case "SHIPPER" -> "Nhân viên giao hàng";
            default -> value.name();
        };
    }

    public String badge(Enum<?> value) {
        if (value == null) return "text-bg-secondary";
        return switch (value.name()) {
            case "ACTIVE", "DELIVERED", "PAID" -> "text-bg-success";
            case "NEW", "CONFIRMED", "ASSIGNED", "PICKED_UP", "SHIPPING" -> "text-bg-primary";
            case "READY_FOR_PICKUP", "RETURN_REQUESTED", "PENDING" -> "text-bg-warning";
            case "BLOCKED", "LOCKED", "CANCELLED", "REJECTED", "FAILED", "DELIVERY_FAILED" -> "text-bg-danger";
            default -> "text-bg-secondary";
        };
    }

    public String date(LocalDateTime value) {
        return value == null ? "—" : value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}

