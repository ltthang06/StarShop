package vn.iotstar.starshop.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerCartLine {

    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;
    private boolean available;
}
