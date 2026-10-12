package vn.iotstar.starshop.config;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
@Component @RequiredArgsConstructor
@org.springframework.core.annotation.Order(-90)
public class AdminBootstrap implements ApplicationRunner {
    private final SecurityUserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    @Value("${BOOTSTRAP_ADMIN_EMAIL:}") private String email;
    @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") private String password;
    @Override @Transactional
    public void run(ApplicationArguments arguments) {
        if(email.isBlank() && password.isBlank()) return;
        if(!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || email.length()>150 || password.length()<12
            || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("Bootstrap Admin cần email hợp lệ và mật khẩu 12–72 ký tự");
        var existing=users.findByEmailIgnoreCase(email);
        if(existing.isPresent()) {
            if(existing.get().getRoles().stream().noneMatch(r -> r.getName()==RoleName.ADMIN))
                throw new IllegalArgumentException("Email bootstrap đã thuộc tài khoản khác; không tự nâng quyền");
            return;
        }
        Role role=roles.findByName(RoleName.ADMIN).orElseGet(() -> {Role r=new Role();r.setName(RoleName.ADMIN);return roles.save(r);});
        User user=new User();user.setFullName("StarShop Admin");user.setEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
        user.setPassword(encoder.encode(password));user.setStatus(UserStatus.ACTIVE);user.getRoles().add(role);users.save(user);
    }
}
