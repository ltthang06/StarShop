package vn.iotstar.starshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.iotstar.starshop.entity.Shop;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Shop> findByIdAndOwnerId(Long id, Long ownerId);
}