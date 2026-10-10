package vn.iotstar.starshop.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;
import lombok.Setter;
import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.entity.Promotion;
import vn.iotstar.starshop.entity.Shop;

@Getter
@Setter
public class PublicShopData {

    private Shop shop;

    private Page<PublicProductCard> products;

    private List<PublicProductCard> bestSellers;

    private List<Category> categories;

    private List<Promotion> promotions;

    private double averageRating;

    private long reviewCount;

    private long productCount;
}