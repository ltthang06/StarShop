package vn.iotstar.starshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.iotstar.starshop.enums.PromotionType;

@Getter
@Setter
public class PromotionRequest {

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Vui lòng chọn loại khuyến mãi")
    private PromotionType type;

    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;

    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    @NotNull(message = "Vui lòng chọn thời gian bắt đầu")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startAt;

    @NotNull(message = "Vui lòng chọn thời gian kết thúc")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endAt;

    private boolean active = true;

    public String getDiscountValueInput() {
        return formatNumber(discountValue);
    }

    public String getMinOrderAmountInput() {
        return formatNumber(minOrderAmount);
    }

    public String getMaxDiscountAmountInput() {
        return formatNumber(maxDiscountAmount);
    }

    private String formatNumber(BigDecimal value) {

        if (value == null) {
            return "";
        }

        return value
                .stripTrailingZeros()
                .toPlainString();
    }
}