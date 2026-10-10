package vn.iotstar.starshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.starshop.entity.ShippingProvider;

public interface CustomerShippingProviderRepository
        extends JpaRepository<ShippingProvider, Long> {

    List<ShippingProvider> findByActiveTrueOrderByBaseFeeAsc();

    Optional<ShippingProvider> findByIdAndActiveTrue(Long id);
}
