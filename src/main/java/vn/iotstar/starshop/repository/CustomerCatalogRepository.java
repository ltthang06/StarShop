package vn.iotstar.starshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.enums.ShopStatus;

public interface CustomerCatalogRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT w.product.id FROM Wishlist w
            WHERE w.product.status = :productStatus
              AND w.product.shop.status = :shopStatus
              AND w.product.category.active = true
              AND w.product.quantity > 0
            GROUP BY w.product.id
            ORDER BY COUNT(w.id) DESC, w.product.id DESC
            """)
    List<Long> findMostWishlistedIds(
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :productStatus
              AND p.shop.status = :shopStatus
              AND p.category.active = true
              AND p.quantity > 0
              AND p.soldCount > 10
            ORDER BY p.soldCount DESC, p.id DESC
            """)
    List<Product> findFeatured(
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :productStatus
              AND p.shop.status = :shopStatus
              AND p.category.active = true
              AND p.quantity > 0
              AND (:keyword = '' OR LOWER(p.name)
                   LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
            """)
    Page<Product> search(
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @EntityGraph(attributePaths = {"category", "shop"})
    @Query("""
            SELECT p FROM Product p
            WHERE p.id = :id
              AND p.status = :productStatus
              AND p.shop.status = :shopStatus
              AND p.category.active = true
              AND p.quantity > 0
            """)
    Optional<Product> findPublicById(
            @Param("id") Long id,
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus);
}
