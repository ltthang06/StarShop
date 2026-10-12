package vn.iotstar.starshop.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.starshop.entity.ProductModeration;
public interface ProductModerationRepository extends JpaRepository<ProductModeration, Long> {
    boolean existsByProductIdAndBlockedTrue(Long productId);
}
