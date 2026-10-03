package vn.iotstar.starshop.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.starshop.entity.ShippingProvider;
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider, Long> {
    List<ShippingProvider> findAllByOrderByNameAsc();
    List<ShippingProvider> findByActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByNameIgnoreCase(String name);
}
