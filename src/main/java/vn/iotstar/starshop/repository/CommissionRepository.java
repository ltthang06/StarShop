package vn.iotstar.starshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.starshop.entity.Commission;

public interface CommissionRepository extends JpaRepository<Commission, Long> {

    List<Commission> findByShopIdAndActiveTrue(Long shopId);

    Optional<Commission> findFirstByShopIdAndActiveTrueOrderByStartAtDesc(Long shopId);
}
