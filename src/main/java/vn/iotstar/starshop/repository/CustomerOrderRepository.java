package vn.iotstar.starshop.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import vn.iotstar.starshop.entity.Order;

public interface CustomerOrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "shop")
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findOwnedForUpdate(
            @Param("id") Long id,
            @Param("userId") Long userId);
}
