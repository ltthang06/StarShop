package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.RoleName;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.CommissionRepository;
import vn.iotstar.starshop.repository.ManagerShopRepository;
import vn.iotstar.starshop.repository.SecurityUserRepository;
import vn.iotstar.starshop.service.ManagerShopService;

@SpringBootTest
class ManagerShopFlowTests {

    @Autowired private ManagerShopService managerShopService;
    @Autowired private ManagerShopRepository shopRepository;
    @Autowired private SecurityUserRepository userRepository;
    @Autowired private CommissionRepository commissionRepository;

    @Test
    void approvalGrantsVendorRoleAndCommissionKeepsHistory() {
        Shop shop = pendingShop();

        assertTrue(managerShopService.search("Shop Test", ShopStatus.PENDING, 0).getContent().stream()
                .anyMatch(found -> found.getId().equals(shop.getId())));

        managerShopService.approve(shop.getId());
        assertEquals(ShopStatus.ACTIVE, shopRepository.findById(shop.getId()).orElseThrow().getStatus());
        User owner = userRepository.findByEmailIgnoreCase(shop.getOwner().getEmail()).orElseThrow();
        assertTrue(owner.getRoles().stream().anyMatch(role -> role.getName() == RoleName.VENDOR));
        assertThrows(IllegalArgumentException.class, () -> managerShopService.approve(shop.getId()));

        managerShopService.setCommission(shop.getId(), new BigDecimal("5.00"));
        managerShopService.setCommission(shop.getId(), new BigDecimal("7.50"));
        assertEquals(new BigDecimal("7.50"), managerShopService.currentCommission(shop.getId())
                .orElseThrow().getRatePercent());
        assertEquals(2, commissionRepository.findAll().stream()
                .filter(commission -> commission.getShop().getId().equals(shop.getId())).count());
        assertEquals(1, commissionRepository.findByShopIdAndActiveTrue(shop.getId()).size());

        managerShopService.block(shop.getId());
        assertEquals(ShopStatus.BLOCKED, shopRepository.findById(shop.getId()).orElseThrow().getStatus());
        managerShopService.reopen(shop.getId());
        assertEquals(ShopStatus.ACTIVE, shopRepository.findById(shop.getId()).orElseThrow().getStatus());
    }

    @Test
    void rejectedShopCannotBeApprovedOrChargedCommission() {
        Shop shop = pendingShop();
        managerShopService.reject(shop.getId());
        assertEquals(ShopStatus.REJECTED, shopRepository.findById(shop.getId()).orElseThrow().getStatus());
        assertThrows(IllegalArgumentException.class, () -> managerShopService.approve(shop.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> managerShopService.setCommission(shop.getId(), new BigDecimal("10")));
        assertFalse(managerShopService.currentCommission(shop.getId()).isPresent());
    }

    private Shop pendingShop() {
        User owner = new User();
        owner.setFullName("Vendor Test");
        owner.setEmail(UUID.randomUUID() + "@example.test");
        owner.setPassword("unused-test-password");
        owner.setStatus(UserStatus.ACTIVE);
        owner = userRepository.save(owner);

        Shop shop = new Shop();
        shop.setName("Shop Test");
        shop.setOwner(owner);
        return shopRepository.save(shop);
    }
}
