package vn.iotstar.starshop.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.Optional;
import vn.iotstar.starshop.entity.Notification;
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Page<Notification> findByUser_EmailIgnoreCaseOrderByCreatedAtDesc(String email, Pageable pageable);
    Optional<Notification> findByIdAndUser_EmailIgnoreCase(Long id, String email);
    long countByUser_EmailIgnoreCaseAndReadStatusFalse(String email);
}
