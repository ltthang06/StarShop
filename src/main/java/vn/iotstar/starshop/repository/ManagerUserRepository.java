package vn.iotstar.starshop.repository;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.*;
public interface ManagerUserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths="roles")
    java.util.Optional<User> findById(Long id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    java.util.Optional<User> findLocked(Long id);
    @Query("select distinct u from User u join u.roles r where r.name = :role and u.status = :status order by u.fullName")
    List<User> findAvailable(@Param("role") RoleName role, @Param("status") UserStatus status);
}
