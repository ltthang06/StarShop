package vn.iotstar.starshop.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PublicProductCard {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal discountPrice;

    private int quantity;

    private int soldCount;

    private double rating;

    private Long categoryId;

    private String categoryName;

    private String imageUrl;
}