package vn.iotstar.starshop.dto;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter
public class ShippingProviderForm {
    private Long id;
    @NotBlank(message="Nhập tên đơn vị vận chuyển") @Size(max=150)
    private String name;
    @Size(max=20) private String phone;
    @NotNull @DecimalMin("0") @Digits(integer=13, fraction=2)
    private BigDecimal baseFee;
    @NotNull @Min(1) @Max(365) private Integer estimatedDays;
    private boolean active = true;
}
