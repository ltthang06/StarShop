package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.*;

@SpringBootTest @Transactional
class ManagerShopScopeTests {
    @Autowired ManagerShopRepository shops;
    @Autowired ManagerUserRepository users;
    @Autowired RoleRepository roles;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;
    @Autowired ManagerShopService shopService;
    @Autowired ManagerCatalogService catalog;
    @Autowired ManagerOperationsService operations;
    @Autowired ManagerReportService reports;
    @Autowired ShopManagerAssignmentService assignments;
    @Autowired CommissionRepository commissions;
    @Autowired CategoryService categoryService;
    @Autowired AdminUserService adminUsers;
    @Autowired ShippingProviderRepository providers;

    @AfterEach void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test void managerCannotReadOrChangeOtherShopsEvenByDirectIdAndAdminCanReassign() {
        var manager=new User(); manager.setEmail(UUID.randomUUID()+"@example.test");manager.setFullName("Manager scope");
        manager.setPassword("unused-test-password-hash"); manager.setStatus(UserStatus.ACTIVE);
        var role=roles.findByName(RoleName.MANAGER).orElseGet(() -> {var r=new Role();r.setName(RoleName.MANAGER);return roles.save(r);});
        manager.getRoles().add(role);manager=users.save(manager);
        var own=new Shop();own.setName("Assigned scope shop");own.setOwner(manager);own.setStatus(ShopStatus.ACTIVE);own=shops.save(own);
        var other=new Shop();other.setName("Unassigned scope shop");other.setOwner(manager);other.setStatus(ShopStatus.ACTIVE);other=shops.save(other);
        login("admin@example.test","ADMIN"); assignments.assign(own.getId(),manager.getId());
        var category=new Category();category.setName("Scope category");category=categories.save(category);
        Product mine=product(own,category), foreign=product(other,category);
        Order foreignOrder=order(manager,other);
        final Long foreignShopId=other.getId(),foreignProductId=foreign.getId(),foreignOrderId=foreignOrder.getId(),ownShopId=own.getId();
        login(manager.getEmail(),"MANAGER");
        assertEquals(1,shopService.search("scope shop",null,0).getTotalElements());
        assertEquals(mine.getId(),catalog.products("Scope product",null,null,0).getContent().get(0).getId());
        assertEquals(1,catalog.products("Scope product",null,null,0).getTotalElements());
        assertEquals(1,reports.report(null,null).shops());
        assertEquals(0,operations.orders(null,0).getTotalElements());
        var buyer=new User();buyer.setEmail(UUID.randomUUID()+"@example.test");buyer.setFullName("Assigned shop buyer");
        buyer.setPassword("unused");buyer.setStatus(UserStatus.ACTIVE);buyer=users.save(buyer);
        Order ownOrder=order(buyer,own);
        assertEquals(1,reports.report(null,null).users());
        assertEquals(1,operations.orders(null,0).getTotalElements());
        assertEquals(ownOrder.getId(),operations.orderDetail(ownOrder.getId()).order().getId());
        var shipperRole=roles.findByName(RoleName.SHIPPER).orElseGet(() -> {var r=new Role();r.setName(RoleName.SHIPPER);return roles.save(r);});
        var shipper=new User();shipper.setEmail(UUID.randomUUID()+"@example.test");shipper.setFullName("Branch shipper");
        shipper.setPassword("unused");shipper.setStatus(UserStatus.ACTIVE);shipper.getRoles().add(shipperRole);shipper=users.save(shipper);
        var provider=new ShippingProvider();provider.setName("Branch carrier "+UUID.randomUUID());provider.setBaseFee(new BigDecimal("20000"));provider.setEstimatedDays(2);provider=providers.save(provider);
        ownOrder.setStatus(OrderStatus.READY_FOR_PICKUP);orders.save(ownOrder);
        operations.assign(ownOrder.getId(),shipper.getId(),provider.getId(),"");
        assertEquals(OrderStatus.ASSIGNED,orders.findById(ownOrder.getId()).orElseThrow().getStatus());
        assertThrows(AccessDeniedException.class,() -> shopService.findById(foreignShopId));
        assertThrows(AccessDeniedException.class,() -> shopService.block(foreignShopId));
        assertThrows(AccessDeniedException.class,() -> catalog.product(foreignProductId));
        assertThrows(AccessDeniedException.class,() -> catalog.moderate(foreignProductId,true,"Không được sửa shop khác","manager"));
        assertThrows(AccessDeniedException.class,() -> operations.orderDetail(foreignOrderId));
        assertThrows(AccessDeniedException.class,() -> operations.assign(foreignOrderId,1L,1L,""));
        assertThrows(AccessDeniedException.class,() -> assignments.assign(ownShopId,null));
        login("admin@example.test","ADMIN");
        assertNotNull(catalog.product(foreignProductId));
        assignments.assign(ownShopId,null);
        login(manager.getEmail(),"MANAGER");assertEquals(0,shopService.search("scope shop",null,0).getTotalElements());
        assertEquals(0,reports.report(null,null).users());
    }

    @Test void branchManagerCanReadCommissionButCannotChangePlatformPoliciesEvenForAssignedShop() {
        var manager=new User();manager.setEmail(UUID.randomUUID()+"@example.test");manager.setFullName("Branch manager");
        manager.setPassword("unused");manager.setStatus(UserStatus.ACTIVE);manager=users.save(manager);
        var shop=new Shop();shop.setName("Branch policy shop");shop.setOwner(manager);shop.setManager(manager);
        shop.setStatus(ShopStatus.ACTIVE);shop=shops.save(shop);
        final Long shopId=shop.getId(),managerId=manager.getId();
        login("admin@example.test","ADMIN");
        shopService.setCommission(shopId,new BigDecimal("5.00"));
        long commissionCount=commissions.count();
        login(manager.getEmail(),"MANAGER");
        assertEquals(0,new BigDecimal("5").compareTo(shopService.currentCommission(shopId).orElseThrow().getRatePercent()));
        assertThrows(AccessDeniedException.class,() -> shopService.setCommission(shopId,new BigDecimal("1")));
        assertEquals(commissionCount,commissions.count());
        assertThrows(AccessDeniedException.class,() -> shopService.approve(shopId));
        assertThrows(AccessDeniedException.class,() -> shopService.reject(shopId));
        assertThrows(AccessDeniedException.class,() -> shopService.block(shopId));
        assertThrows(AccessDeniedException.class,() -> shopService.reopen(shopId));
        assertThrows(AccessDeniedException.class,() -> categoryService.save(new Category()));
        assertThrows(AccessDeniedException.class,() -> categoryService.toggleActive(1L));
        assertThrows(AccessDeniedException.class,() -> catalog.promotions("",0));
        assertThrows(AccessDeniedException.class,() -> catalog.savePromotion(null,new vn.iotstar.starshop.dto.PromotionRequest()));
        assertThrows(AccessDeniedException.class,() -> catalog.togglePromotion(1L,false));
        assertThrows(AccessDeniedException.class,() -> operations.users("",0));
        assertThrows(AccessDeniedException.class,() -> assignments.candidates());
        assertThrows(AccessDeniedException.class,() -> operations.setUserLocked(managerId,true,"manager@example.test",true));
        assertThrows(AccessDeniedException.class,() -> operations.saveProvider(new vn.iotstar.starshop.dto.ShippingProviderForm()));
        assertThrows(AccessDeniedException.class,() -> adminUsers.setOperationalRole(managerId,RoleName.SHIPPER,true,"manager@example.test"));
        assertEquals(ShopStatus.ACTIVE,shops.findById(shopId).orElseThrow().getStatus());
        login("admin@example.test","ADMIN");
        shopService.setCommission(shopId,new BigDecimal("7.50"));
        assertEquals(0,new BigDecimal("7.5").compareTo(shopService.currentCommission(shopId).orElseThrow().getRatePercent()));
    }

    private void login(String email,String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email,"",AuthorityUtils.createAuthorityList("ROLE_"+role)));
    }
    private Product product(Shop shop,Category category) {
        var p=new Product();p.setName("Scope product");p.setDescription("Scope security test product");p.setShop(shop);p.setCategory(category);p.setPrice(new BigDecimal("200000"));p.setQuantity(5);return products.save(p);
    }
    private Order order(User user,Shop shop) {
        var o=new Order();o.setUser(user);o.setShop(shop);o.setReceiverName("Buyer");o.setReceiverPhone("0900000000");o.setShippingAddress("Test address");
        o.setSubtotal(new BigDecimal("200000"));o.setTotalAmount(new BigDecimal("200000"));o.setPaymentMethod(PaymentMethod.COD);return orders.save(o);
    }
}
