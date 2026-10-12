package vn.iotstar.starshop.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerOrderLine {

    private Long detailId;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal subtotal;
    private boolean canReview;
}
