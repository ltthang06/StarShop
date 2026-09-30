package vn.iotstar.starshop.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.iotstar.starshop.entity.Promotion;
import vn.iotstar.starshop.enums.PromotionScope;

@Repository
public interface PromotionRepository
        extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCodeIgnoreCase(String code);

    Optional<Promotion> findByIdAndShop_IdAndScope(
            Long id,
            Long shopId,
            PromotionScope scope
    );

    @Query("""
        SELECT p
        FROM Promotion p
        WHERE p.shop.id = :shopId
          AND p.scope = :scope
          AND (
                :keyword = ''
                OR LOWER(p.code)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.name)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
          AND (:active IS NULL OR p.active = :active)
        """)
    Page<Promotion> searchVendorPromotions(
            @Param("shopId") Long shopId,
            @Param("scope") PromotionScope scope,
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            Pageable pageable
    );
}