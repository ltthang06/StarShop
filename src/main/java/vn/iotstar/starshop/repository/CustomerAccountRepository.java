package vn.iotstar.starshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.starshop.entity.User;

public interface CustomerAccountRepository
        extends JpaRepository<User, Long> {

    boolean existsByPhoneAndIdNot(String phone, Long id);
}
