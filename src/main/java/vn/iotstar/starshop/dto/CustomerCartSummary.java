package vn.iotstar.starshop.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerCartSummary {

    private List<CustomerCartLine> lines;
    private BigDecimal subtotal;
    private boolean readyToCheckout;
}
