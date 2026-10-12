package vn.iotstar.starshop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerReviewTarget {

    private Long orderId;
    private Long detailId;
    private String productName;
}
