package vn.iotstar.starshop.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.PublicProductCard;
import vn.iotstar.starshop.dto.PublicShopData;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.ProductImage;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.enums.PromotionScope;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.repository.ProductImageRepository;
import vn.iotstar.starshop.repository.PublicShopRepository;
import vn.iotstar.starshop.repository.ShopRepository;
import vn.iotstar.starshop.service.PublicShopService;

@Service
@RequiredArgsConstructor
public class PublicShopServiceImpl
        implements PublicShopService {

    private static final ZoneId VIETNAM_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final ShopRepository shopRepository;

    private final PublicShopRepository publicShopRepository;

    private final ProductImageRepository productImageRepository;

    @Override
    @Transactional(readOnly = true)
    public PublicShopData getPublicShop(
            Long shopId,
            String keyword,
            Long categoryId,
            int page,
            int size) {

        Shop shop = shopRepository
                .findById(shopId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy cửa hàng."
                        )
                );

        if (shop.getStatus() != ShopStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Cửa hàng hiện chưa hoạt động."
            );
        }

        String searchKeyword =
                keyword == null
                        ? ""
                        : keyword.trim();

        int safePage = Math.max(
                page,
                0
        );

        int safeSize =
                size <= 0
                        ? 12
                        : Math.min(
                                size,
                                24
                        );

        PageRequest pageable =
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<Product> productPage =
                publicShopRepository
                        .searchPublicProducts(
                                shopId,
                                ShopStatus.ACTIVE,
                                ProductStatus.ACTIVE,
                                searchKeyword,
                                categoryId,
                                pageable
                        );

        Page<PublicProductCard> productCards =
                productPage.map(
                        this::toProductCard
                );

        List<PublicProductCard> bestSellers =
                publicShopRepository
                        .findPublicProductsForBestSeller(
                                shopId,
                                ShopStatus.ACTIVE,
                                ProductStatus.ACTIVE
                        )
                        .stream()
                        .map(this::toProductCard)
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                PublicProductCard::getSoldCount
                                        )
                                        .reversed()
                                        .thenComparing(
                                                Comparator.comparingDouble(
                                                        PublicProductCard::getRating
                                                ).reversed()
                                        )
                                        .thenComparing(
                                                PublicProductCard::getId,
                                                Comparator.reverseOrder()
                                        )
                        )
                        .limit(5)
                        .toList();

        LocalDateTime now =
                LocalDateTime.now(
                        VIETNAM_ZONE
                );

        Double averageRating =
                publicShopRepository
                        .findAverageShopRating(
                                shopId
                        );

        PublicShopData data =
                new PublicShopData();

        data.setShop(
                shop
        );

        data.setProducts(
                productCards
        );

        data.setBestSellers(
                bestSellers
        );

        data.setCategories(
                publicShopRepository
                        .findPublicCategories(
                                shopId,
                                ShopStatus.ACTIVE,
                                ProductStatus.ACTIVE
                        )
        );

        data.setPromotions(
                publicShopRepository
                        .findActivePromotions(
                                shopId,
                                PromotionScope.SHOP,
                                now
                        )
        );

        data.setAverageRating(
                averageRating == null
                        ? 0
                        : averageRating
        );

        data.setReviewCount(
                publicShopRepository
                        .countShopReviews(
                                shopId
                        )
        );

        data.setProductCount(
                publicShopRepository
                        .countPublicProducts(
                                shopId,
                                ShopStatus.ACTIVE,
                                ProductStatus.ACTIVE
                        )
        );

        return data;
    }

    private PublicProductCard toProductCard(
            Product product) {

        String imageUrl =
                productImageRepository
                        .findFirstByProductIdOrderByPrimaryImageDescIdAsc(
                                product.getId()
                        )
                        .map(
                                ProductImage::getImageUrl
                        )
                        .orElse(null);

        Long soldQuantity =
                publicShopRepository
                        .findSoldQuantityByProductId(
                                product.getId(),
                                OrderStatus.DELIVERED
                        );

        int soldCount =
                soldQuantity == null
                        ? 0
                        : soldQuantity.intValue();

        Double averageRating =
                publicShopRepository
                        .findAverageProductRating(
                                product.getId()
                        );

        double rating =
                averageRating == null
                        ? 0
                        : averageRating;

        return new PublicProductCard(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getDiscountPrice(),
                product.getQuantity(),
                soldCount,
                rating,
                product.getCategory().getId(),
                product.getCategory().getName(),
                imageUrl
        );
    }
}