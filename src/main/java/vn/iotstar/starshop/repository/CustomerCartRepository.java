package vn.iotstar.starshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.starshop.entity.Cart;

public interface CustomerCartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);
}
