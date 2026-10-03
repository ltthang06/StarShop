package vn.iotstar.starshop.repository;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.*;
public interface ManagerUserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("select distinct u from User u join u.roles r where r.name = :role and u.status = :status order by u.fullName")
    List<User> findAvailable(@Param("role") RoleName role, @Param("status") UserStatus status);
}
