package vn.iotstar.starshop.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.iotstar.starshop.enums.OrderStatus;

@Getter
@AllArgsConstructor
public class CustomerOrderCard {

    private Long id;
    private String shopName;
    private String createdAt;
    private OrderStatus status;
    private String shippingAddress;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private List<CustomerOrderLine> lines;
    private boolean canCancel;
    private boolean canReturn;
}
