package vn.iotstar.starshop.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.entity.Commission;
import vn.iotstar.starshop.entity.Role;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.RoleName;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.repository.CommissionRepository;
import vn.iotstar.starshop.repository.ManagerShopRepository;
import vn.iotstar.starshop.repository.RoleRepository;
import vn.iotstar.starshop.repository.SecurityUserRepository;
import vn.iotstar.starshop.service.ManagerShopService;

@Service
@RequiredArgsConstructor
public class ManagerShopServiceImpl implements ManagerShopService {

    private final ManagerShopRepository shopRepository;
    private final CommissionRepository commissionRepository;
    private final RoleRepository roleRepository;
    private final SecurityUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Shop> search(String keyword, ShopStatus status, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return shopRepository.search(keyword == null ? "" : keyword.trim(), status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Shop findById(Long id) {
        return shopRepository.findWithOwner(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng"));
    }

    @Override
    @Transactional(readOnly = true)
    public long countPending() {
        return shopRepository.countByStatus(ShopStatus.PENDING);
    }

    @Override
    @Transactional
    public void approve(Long id) {
        Shop shop = lockedShop(id);
        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể duyệt cửa hàng đang chờ");
        }
        shop.setStatus(ShopStatus.ACTIVE);
        User owner = shop.getOwner();
        if (owner.getRoles().stream().noneMatch(role -> role.getName() == RoleName.VENDOR)) {
            Role vendorRole = roleRepository.findByName(RoleName.VENDOR).orElseGet(() -> {
                Role role = new Role();
                role.setName(RoleName.VENDOR);
                return roleRepository.save(role);
            });
            owner.getRoles().add(vendorRole);
            userRepository.save(owner);
        }
        shopRepository.save(shop);
    }

    @Override
    @Transactional
    public void reject(Long id) {
        Shop shop = lockedShop(id);
        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể từ chối cửa hàng đang chờ");
        }
        shop.setStatus(ShopStatus.REJECTED);
        shopRepository.save(shop);
    }

    @Override
    @Transactional
    public void block(Long id) {
        Shop shop = lockedShop(id);
        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new IllegalArgumentException("Chỉ có thể khóa cửa hàng đang hoạt động");
        }
        shop.setStatus(ShopStatus.BLOCKED);
        shopRepository.save(shop);
    }

    @Override
    @Transactional
    public void reopen(Long id) {
        Shop shop = lockedShop(id);
        if (shop.getStatus() != ShopStatus.BLOCKED) {
            throw new IllegalArgumentException("Chỉ có thể mở lại cửa hàng đã khóa");
        }
        shop.setStatus(ShopStatus.ACTIVE);
        shopRepository.save(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Commission> currentCommission(Long shopId) {
        return commissionRepository.findFirstByShopIdAndActiveTrueOrderByStartAtDesc(shopId);
    }

    @Override
    @Transactional
    public void setCommission(Long shopId, BigDecimal ratePercent) {
        if (ratePercent == null || ratePercent.scale() > 2
                || ratePercent.compareTo(BigDecimal.ZERO) < 0
                || ratePercent.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("Chiết khấu phải từ 0 đến 100% và có tối đa 2 chữ số thập phân");
        }
        Shop shop = lockedShop(shopId);
        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new IllegalArgumentException("Chỉ đặt chiết khấu cho cửa hàng đang hoạt động");
        }
        LocalDateTime now = LocalDateTime.now();
        for (Commission current : commissionRepository.findByShopIdAndActiveTrue(shopId)) {
            current.setActive(false);
            current.setEndAt(now);
            commissionRepository.save(current);
        }
        Commission commission = new Commission();
        commission.setShop(shop);
        commission.setRatePercent(ratePercent);
        commission.setStartAt(now);
        commission.setActive(true);
        commissionRepository.save(commission);
    }

    private Shop lockedShop(Long id) {
        return shopRepository.findForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cửa hàng"));
    }
}
