package vn.iotstar.starshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.starshop.entity.User;

public interface SecurityUserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmailIgnoreCase(String email);
}
