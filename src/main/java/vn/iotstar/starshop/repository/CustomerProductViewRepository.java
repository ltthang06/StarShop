package vn.iotstar.starshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.starshop.entity.ProductView;

public interface CustomerProductViewRepository
        extends JpaRepository<ProductView, Long> {

    List<ProductView> findByUserIdOrderByViewedAtDesc(
            Long userId, Pageable pageable);

    Optional<ProductView> findByUserIdAndProductId(
            Long userId, Long productId);
}
