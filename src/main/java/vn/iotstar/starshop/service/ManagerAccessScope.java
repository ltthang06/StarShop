package vn.iotstar.starshop.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.repository.ManagerShopRepository;
import vn.iotstar.starshop.repository.ManagerUserRepository;
import vn.iotstar.starshop.entity.User;


@Service @RequiredArgsConstructor
public class ManagerAccessScope {
    private final ManagerShopRepository shops;
    private final ManagerUserRepository users;

    public void requireAdmin() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !auth.isAuthenticated() || auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
            throw new AccessDeniedException("Chỉ Admin được thay đổi chính sách toàn hệ thống");
    }

    public String managerEmail() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if (auth==null) return null;
        boolean admin=auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (admin) return null;
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) return auth.getName();
        throw new AccessDeniedException("Không có quyền quản trị cửa hàng");
    }

    public List<Shop> visibleShops() {
        String email=managerEmail();
        return email==null ? shops.findAllByOrderByNameAsc() : shops.findByManager_EmailIgnoreCaseOrderByNameAsc(email);
    }

    public void requireShop(Shop shop) {
        String email=managerEmail();
        if (email!=null && (shop.getManager()==null || !email.equalsIgnoreCase(shop.getManager().getEmail())))
            throw new AccessDeniedException("Cửa hàng chưa được phân công cho bạn");
    }

    public void requireManagedUser(User user) {
        String email = managerEmail();
        if (email != null && (user.getRoles().stream().anyMatch(role ->
                role.getName() == vn.iotstar.starshop.enums.RoleName.ADMIN
                || role.getName() == vn.iotstar.starshop.enums.RoleName.MANAGER)
                || users.countUserInManagedShops(user.getId(), email) == 0))
            throw new AccessDeniedException("Tài khoản không thuộc cửa hàng được phân công");
    }
}
