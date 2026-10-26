package vn.iotstar.starshop.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.iotstar.starshop.entity.VnpayPayment;

public interface VnpayPaymentRepository extends JpaRepository<VnpayPayment,String> {
    @Query("select p.reference from VnpayPayment p where p.state=:pending "
            + "and (p.expiresAt < :now or p.returnReceivedAt is not null) "
            + "and (p.lastCheckedAt is null or p.lastCheckedAt < :cutoff) order by p.createdAt")
    List<String> findDue(@Param("pending") VnpayPayment.State pending, @Param("now") java.time.LocalDateTime now,
            @Param("cutoff") java.time.LocalDateTime cutoff, org.springframework.data.domain.Pageable pageable);

    @Query("select p.reference from VnpayPayment p where p.state in :states "
            + "order by p.createdAt")
    org.springframework.data.domain.Page<String> findUnresolved(@Param("states") java.util.Collection<VnpayPayment.State> states, org.springframework.data.domain.Pageable pageable);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from VnpayPayment p where p.reference=:reference")
    Optional<VnpayPayment> findForUpdate(@Param("reference") String reference);
    @Query("select p from VnpayPayment p join p.orders o where o.id=:orderId and lower(p.user.email)=lower(:email)")
    Optional<VnpayPayment> findOwnedByOrder(@Param("orderId") Long orderId,@Param("email") String email);
}
