package vn.iotstar.starshop.repository;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.code) = LOWER(:code)")
    Optional<Promotion> findLockedByCodeIgnoreCase(@Param("code") String code);
}
