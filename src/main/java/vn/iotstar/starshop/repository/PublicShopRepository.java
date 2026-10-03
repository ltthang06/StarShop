package vn.iotstar.starshop.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.Promotion;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.enums.PromotionScope;
import vn.iotstar.starshop.enums.ShopStatus;

@Repository
public interface PublicShopRepository
        extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {
            "category",
            "shop"
    })
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.shop.id = :shopId
          AND p.shop.status = :shopStatus
          AND p.status = :productStatus
          AND p.quantity > 0
          AND (
                :keyword = ''
                OR LOWER(p.name)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.description)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
          AND (
                :categoryId IS NULL
                OR p.category.id = :categoryId
              )
        """)
    Page<Product> searchPublicProducts(
            @Param("shopId") Long shopId,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("productStatus") ProductStatus productStatus,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "category",
            "shop"
    })
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.shop.id = :shopId
          AND p.shop.status = :shopStatus
          AND p.status = :productStatus
          AND p.quantity > 0
        """)
    List<Product> findPublicProductsForBestSeller(
            @Param("shopId") Long shopId,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("productStatus") ProductStatus productStatus
    );

    @Query("""
        SELECT DISTINCT c
        FROM Product p
        JOIN p.category c
        WHERE p.shop.id = :shopId
          AND p.shop.status = :shopStatus
          AND p.status = :productStatus
          AND p.quantity > 0
          AND c.active = true
        ORDER BY c.name ASC
        """)
    List<Category> findPublicCategories(
            @Param("shopId") Long shopId,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("productStatus") ProductStatus productStatus
    );

    @Query("""
        SELECT p
        FROM Promotion p
        WHERE p.shop.id = :shopId
          AND p.scope = :scope
          AND p.active = true
          AND p.quantity > 0
          AND p.startAt <= :now
          AND p.endAt >= :now
        ORDER BY p.endAt ASC
        """)
    List<Promotion> findActivePromotions(
            @Param("shopId") Long shopId,
            @Param("scope") PromotionScope scope,
            @Param("now") LocalDateTime now
    );

    @Query("""
        SELECT COALESCE(AVG(r.rating), 0)
        FROM Review r
        WHERE r.product.shop.id = :shopId
        """)
    Double findAverageShopRating(
            @Param("shopId") Long shopId
    );

    @Query("""
        SELECT COUNT(r)
        FROM Review r
        WHERE r.product.shop.id = :shopId
        """)
    long countShopReviews(
            @Param("shopId") Long shopId
    );

    @Query("""
        SELECT COALESCE(AVG(r.rating), 0)
        FROM Review r
        WHERE r.product.id = :productId
        """)
    Double findAverageProductRating(
            @Param("productId") Long productId
    );

    @Query("""
        SELECT COALESCE(SUM(od.quantity), 0)
        FROM OrderDetail od
        WHERE od.product.id = :productId
          AND od.order.status = :orderStatus
        """)
    Long findSoldQuantityByProductId(
            @Param("productId") Long productId,
            @Param("orderStatus") OrderStatus orderStatus
    );

    @Query("""
        SELECT COUNT(p)
        FROM Product p
        WHERE p.shop.id = :shopId
          AND p.shop.status = :shopStatus
          AND p.status = :productStatus
          AND p.quantity > 0
        """)
    long countPublicProducts(
            @Param("shopId") Long shopId,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("productStatus") ProductStatus productStatus
    );
}