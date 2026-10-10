package vn.iotstar.starshop.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import vn.iotstar.starshop.entity.Promotion;

public interface CustomerPromotionRepository
        extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCodeIgnoreCase(String code);

    @Query("""
            SELECT p FROM Promotion p LEFT JOIN FETCH p.shop
            WHERE p.active = true AND p.quantity > 0
            AND p.startAt <= :now AND p.endAt >= :now
            ORDER BY p.endAt ASC, p.id ASC
            """)
    List<Promotion> findAvailable(@Param("now") LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.code) = LOWER(:code)")
    Optional<Promotion> findLockedByCodeIgnoreCase(@Param("code") String code);
}
