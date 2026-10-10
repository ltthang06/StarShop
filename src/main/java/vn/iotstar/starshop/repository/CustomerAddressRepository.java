package vn.iotstar.starshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.starshop.entity.Address;

public interface CustomerAddressRepository
        extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByDefaultAddressDescIdAsc(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);
}
