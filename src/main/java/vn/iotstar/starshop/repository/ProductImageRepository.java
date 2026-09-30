package vn.iotstar.starshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.iotstar.starshop.entity.ProductImage;

@Repository
public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByPrimaryImageDescIdAsc(
            Long productId);

    Optional<ProductImage> findByIdAndProductId(
            Long imageId,
            Long productId);

    Optional<ProductImage>
            findFirstByProductIdOrderByPrimaryImageDescIdAsc(
                    Long productId);

    long countByProductId(Long productId);
}