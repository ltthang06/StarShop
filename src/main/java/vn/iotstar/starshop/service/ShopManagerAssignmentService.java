package vn.iotstar.starshop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;

@Service @RequiredArgsConstructor
public class ShopManagerAssignmentService {
    private final ManagerShopRepository shops;
    private final ManagerUserRepository users;

    @Transactional(readOnly=true)
    public java.util.List<vn.iotstar.starshop.entity.User> candidates() {
        requireAdmin();
        return users.findAll().stream().filter(u -> u.getStatus()==UserStatus.ACTIVE
                && u.getRoles().stream().anyMatch(r -> r.getName()==RoleName.MANAGER)).toList();
    }

    @Transactional
    public void assign(Long shopId, Long managerId) {
        requireAdmin();
        var shop=shops.findForUpdate(shopId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng"));
        if(managerId==null) { shop.setManager(null); return; }
        var manager=users.findById(managerId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quản lý"));
        if(manager.getStatus()!=UserStatus.ACTIVE || manager.getRoles().stream().noneMatch(r -> r.getName()==RoleName.MANAGER))
            throw new IllegalArgumentException("Chọn tài khoản MANAGER đang hoạt động");
        shop.setManager(manager);
    }

    private void requireAdmin() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
            throw new AccessDeniedException("Chỉ Admin được phân công quản lý");
    }
}
