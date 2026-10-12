package vn.iotstar.starshop.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.entity.Role;
import vn.iotstar.starshop.repository.*;
@Service @RequiredArgsConstructor
public class AdminUserService {
    private final ManagerUserRepository users;
    private final RoleRepository roles;
    @Transactional
    public void setOperationalRole(Long id,RoleName role,boolean enabled,String actor) {
        if(role!=RoleName.SHIPPER && role!=RoleName.MANAGER)
            throw new IllegalArgumentException("Chỉ cấp/gỡ vai trò MANAGER hoặc SHIPPER; VENDOR được cấp qua duyệt shop");
        var user=users.findLocked(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        if(user.getEmail().equalsIgnoreCase(actor) || user.getRoles().stream().anyMatch(r -> r.getName()==RoleName.ADMIN))
            throw new IllegalArgumentException("Không thay đổi quyền của chính mình hoặc Admin");
        if(user.getStatus()!=UserStatus.ACTIVE) throw new IllegalArgumentException("Chỉ cấp quyền cho tài khoản hoạt động");
        if(enabled) {
            Role assigned=roles.findByName(role).orElseGet(() -> {Role r=new Role();r.setName(role);return roles.save(r);});
            user.getRoles().add(assigned);
        } else user.getRoles().removeIf(r -> r.getName()==role);
    }
}
