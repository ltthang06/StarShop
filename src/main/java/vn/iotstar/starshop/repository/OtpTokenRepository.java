package vn.iotstar.starshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.starshop.entity.OtpToken;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    List<OtpToken> findByUserIdAndUsedFalse(Long userId);

    Optional<OtpToken> findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(
            Long userId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpToken> findFirstByUserIdAndUsedFalseOrderByCreatedAtDescIdDesc(Long userId);

    Optional<OtpToken> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
