package vn.iotstar.starshop.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerOrderLine {

    private String productName;
    private int quantity;
    private BigDecimal subtotal;
}
