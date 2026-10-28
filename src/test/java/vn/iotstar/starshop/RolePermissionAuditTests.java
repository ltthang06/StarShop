package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.*;

@SpringBootTest(properties="starshop.sitemesh.enabled=false")
@AutoConfigureMockMvc
@Transactional
class RolePermissionAuditTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired ShopRepository shops;
    @Autowired ProductRepository products;
    @Autowired CategoryRepository categories;
    @Autowired OrderRepository orders;
    @Autowired ShipperAssignmentRepository assignments;
    @Autowired ShopService shopService;
    @Autowired ProductService productService;
    @Autowired OrderService orderService;
    @Autowired PromotionService promotionService;
    @Autowired ShipperService shipperService;
    @Autowired CustomerAccountService customerAccounts;
    @Autowired CustomerAddressRepository addresses;
    @Autowired CustomerOrderService customerOrders;
    @Autowired CustomerReviewService reviewService;
    @Autowired OrderDetailRepository details;
    @Autowired ManagerOperationsService management;
    @Autowired ManagerShopService managedShops;

    @Test void managerCanManageAssignedCustomersAndAdminCanReadAllManagers() throws Exception {
        User manager=account(RoleName.MANAGER), otherManager=account(RoleName.MANAGER);
        User buyer=account(RoleName.USER), unrelated=account(RoleName.USER), admin=account(RoleName.ADMIN);
        Shop own=shop(account(RoleName.VENDOR)); own.setManager(manager); shops.save(own);
        order(buyer,own); order(otherManager,own);
        var actor=user(manager.getEmail()).roles("MANAGER");
        var list=mvc.perform(get("/manager/users").with(actor).param("keyword",buyer.getEmail()))
            .andExpect(status().isOk()).andReturn();
        var page=(org.springframework.data.domain.Page<?>)list.getModelAndView().getModel().get("users");
        assertEquals(1,page.getTotalElements());
        mvc.perform(get("/manager/users/"+buyer.getId()).with(actor)).andExpect(status().isOk());
        mvc.perform(get("/manager/users/"+otherManager.getId()).with(actor)).andExpect(status().isForbidden());
        mvc.perform(post("/manager/users/"+unrelated.getId()+"/lock").with(actor).with(csrf()).param("locked","true"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/manager/users/"+buyer.getId()+"/lock").with(actor).with(csrf()).param("locked","true"))
            .andExpect(status().is3xxRedirection());
        assertEquals(UserStatus.LOCKED,users.findById(buyer.getId()).orElseThrow().getStatus());
        assertEquals(UserStatus.ACTIVE,users.findById(unrelated.getId()).orElseThrow().getStatus());
        mvc.perform(get("/manager/users/"+otherManager.getId()).with(user(admin.getEmail()).roles("ADMIN")))
            .andExpect(status().isOk());
    }

    @Test void managerCanSetCommissionForAssignedShopAndCannotChangeOtherShops() throws Exception {
        User manager = account(RoleName.MANAGER), vendor = account(RoleName.VENDOR);
        Shop own = shop(vendor); own.setManager(manager); shops.save(own);
        Shop other = shop(vendor);
        var actor = user(manager.getEmail()).roles("MANAGER");
        mvc.perform(post("/manager/shops/"+own.getId()+"/commission").with(actor).with(csrf()).param("ratePercent","7.5"))
            .andExpect(status().is3xxRedirection());
        mvc.perform(post("/manager/shops/"+other.getId()+"/commission").with(actor).with(csrf()).param("ratePercent","7.5"))
            .andExpect(status().isForbidden());
    }

    @Test void managerCanSaveCategoriesPromotionsAndShippingProviders() throws Exception {
        User manager = account(RoleName.MANAGER);
        var actor = user(manager.getEmail()).roles("MANAGER");
        mvc.perform(post("/manager/categories/save").with(actor).with(csrf())
            .param("name","Audit category "+UUID.randomUUID()).param("active","true"))
            .andExpect(status().is3xxRedirection());
        mvc.perform(post("/manager/shipping-providers").with(actor).with(csrf())
            .param("name","Audit carrier "+UUID.randomUUID()).param("baseFee","25000").param("estimatedDays","2").param("active","true"))
            .andExpect(status().is3xxRedirection());
        for (String type : new String[]{"PERCENT","FREE_SHIPPING"}) {
            mvc.perform(post("/manager/promotions/save").with(actor).with(csrf())
                .param("code","AUDIT"+UUID.randomUUID().toString().substring(0,8)).param("name","Audit voucher")
                .param("type",type).param("discountValue","10").param("quantity","10").param("active","true")
                .param("startAt","2026-10-01T00:00").param("endAt","2030-10-01T00:00"))
                .andExpect(status().is3xxRedirection());
        }
    }

    @Test void managerCannotReadAnotherManagersPrivateProfileById() throws Exception {
        User manager = account(RoleName.MANAGER), other = account(RoleName.MANAGER);
        mvc.perform(get("/manager/users/" + other.getId()).with(user(manager.getEmail()).roles("MANAGER")))
            .andExpect(status().isForbidden());
    }

    @Test void managerUserSearchDoesNotIncludeAnUnrelatedManager() {
        User manager = account(RoleName.MANAGER), other = account(RoleName.MANAGER);
        var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
        var previous = context.getAuthentication();
        try {
            context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                manager.getEmail(),"",org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_MANAGER")));
            assertEquals(0,management.users(other.getEmail(),0).getTotalElements());
        } finally { context.setAuthentication(previous); }
    }

    @Test void customersCannotReadOrChangeAnotherCustomersAddressOrOrder() {
        User buyer = account(RoleName.USER), stranger = account(RoleName.USER);
        var address = new Address(); address.setUser(buyer); address.setRecipientName("Private buyer");
        address.setPhone("0900000000"); address.setAddressLine("Private address");
        Long addressId = addresses.save(address).getId();
        Order order = order(buyer,shop(account(RoleName.VENDOR)));
        assertThrows(IllegalArgumentException.class, () -> customerAccounts.address(stranger.getEmail(),addressId));
        assertThrows(IllegalArgumentException.class, () -> customerAccounts.deleteAddress(stranger.getEmail(),addressId));
        assertThrows(IllegalArgumentException.class, () -> customerAccounts.setDefault(stranger.getEmail(),addressId));
        assertThrows(IllegalArgumentException.class, () -> customerOrders.cancel(stranger.getEmail(),order.getId()));
        assertThrows(IllegalArgumentException.class, () -> customerOrders.requestReturn(stranger.getEmail(),order.getId()));
        assertEquals(OrderStatus.NEW,orders.findById(order.getId()).orElseThrow().getStatus());
        assertTrue(addresses.findById(addressId).isPresent());
    }

    @Test void onlyBuyerOfDeliveredProductCanReviewAndTextMustHaveAtLeast50Characters() throws Exception {
        User buyer = account(RoleName.USER), stranger = account(RoleName.USER);
        Shop shop = shop(account(RoleName.VENDOR));
        var category = new Category(); category.setName("Review audit " + UUID.randomUUID()); category = categories.save(category);
        var product = new Product(); product.setName("Review audit flower"); product.setDescription("Review audit flower");
        product.setPrice(new BigDecimal("100000")); product.setQuantity(10); product.setShop(shop); product.setCategory(category);
        product = products.save(product);
        Order order = order(buyer,shop);
        var line = new OrderDetail(); line.setOrder(order); line.setProduct(product); line.setQuantity(1);
        line.setUnitPrice(product.getPrice()); line.setSubtotal(product.getPrice()); Long detailId = details.save(line).getId();
        assertThrows(IllegalArgumentException.class, () -> reviewService.target(buyer.getEmail(),order.getId(),detailId));
        order.setStatus(OrderStatus.DELIVERED); orders.save(order);
        assertThrows(IllegalArgumentException.class, () -> reviewService.submit(stranger.getEmail(),order.getId(),detailId,5,"a".repeat(50),null,null));
        assertThrows(IllegalArgumentException.class, () -> reviewService.submit(buyer.getEmail(),order.getId(),detailId,5,"a".repeat(49),null,null));
        reviewService.submit(buyer.getEmail(),order.getId(),detailId,5,"a".repeat(50),null,null);
        assertThrows(IllegalArgumentException.class, () -> reviewService.submit(buyer.getEmail(),order.getId(),detailId,5,"a".repeat(50),null,null));
    }

    @Test void guestsMustSignInForPrivateRoutesAndCannotSubmitManagementActions() throws Exception {
        for (String path : new String[]{"/", "/products", "/products/top"})
            mvc.perform(get(path)).andExpect(status().isOk());
        for (String path : new String[]{"/cart", "/checkout", "/orders", "/profile", "/wishlist",
                "/manager", "/vendor/shop/register", "/shipper/orders"})
            mvc.perform(get(path)).andExpect(status().is3xxRedirection());
        mvc.perform(post("/manager/shops/1/commission").with(csrf()).param("ratePercent","5"))
            .andExpect(status().is3xxRedirection());
    }

    @Test void eachDatabaseRoleCanOnlyOpenItsWorkspaceAndVendorInheritsCustomerFunctions() throws Exception {
        for (RoleName role : RoleName.values()) {
            User account = account(role);
            var actor = user(account.getEmail()).roles(role.name());
            for (String path : new String[]{"/", "/products", "/products/top", "/profile"})
                mvc.perform(get(path).with(actor)).andExpect(status().isOk());
            boolean buyer = role == RoleName.USER || role == RoleName.VENDOR;
            for (String path : new String[]{"/cart", "/orders", "/wishlist", "/recently-viewed"})
                if (buyer) mvc.perform(get(path).with(actor)).andExpect(status().isOk());
            boolean management = role == RoleName.MANAGER || role == RoleName.ADMIN;
            for (String path : new String[]{"/manager/shops", "/manager/users", "/manager/products",
                    "/manager/categories", "/manager/promotions", "/manager/shipping-providers", "/manager/reports"})
                mvc.perform(get(path).with(actor)).andExpect(management ? status().isOk() : status().isForbidden());
            mvc.perform(get("/shipper/orders").with(actor))
                .andExpect(role == RoleName.SHIPPER || role == RoleName.ADMIN ? status().isOk() : status().isForbidden());
            mvc.perform(get("/vendor/shop/register").with(actor))
                .andExpect(buyer || role == RoleName.ADMIN ? status().isOk() : status().isForbidden());
            if (!management) {
                mvc.perform(post("/manager/shops/1/commission").with(actor).with(csrf()).param("ratePercent","5"))
                    .andExpect(status().isForbidden());
                mvc.perform(post("/manager/products/1/moderate").with(actor).with(csrf()).param("blocked","true"))
                    .andExpect(status().isForbidden());
            }
        }
    }

    @Test void vendorCannotReadOrModifyAnotherVendorsShopProductOrderOrPromotion() {
        User first = account(RoleName.VENDOR), second = account(RoleName.VENDOR);
        Shop shop = shop(first);
        var category = new Category(); category.setName("Ownership " + UUID.randomUUID()); category = categories.save(category);
        var product = new Product(); product.setName("Ownership flower"); product.setDescription("Ownership flower");
        product.setPrice(new BigDecimal("100000")); product.setQuantity(10); product.setShop(shop); product.setCategory(category);
        product = products.save(product);
        Order order = order(first,shop);
        final Long productId = product.getId();
        assertEquals(shop.getId(),shopService.getShopByOwner(shop.getId(),first.getId()).getId());
        assertThrows(IllegalArgumentException.class, () -> shopService.getShopByOwner(shop.getId(),second.getId()));
        assertThrows(IllegalArgumentException.class, () -> productService.getProductByOwner(productId,shop.getId(),second.getId()));
        assertThrows(IllegalArgumentException.class, () -> productService.deactivateProduct(productId,shop.getId(),second.getId()));
        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderByOwner(order.getId(),shop.getId(),second.getId()));
        assertThrows(IllegalArgumentException.class, () -> orderService.confirmOrder(order.getId(),shop.getId(),second.getId()));
        assertThrows(IllegalArgumentException.class, () -> promotionService.searchVendorPromotions(shop.getId(),second.getId(),"",null,0,10));
        assertEquals(ProductStatus.ACTIVE,products.findById(productId).orElseThrow().getStatus());
        assertEquals(OrderStatus.NEW,orders.findById(order.getId()).orElseThrow().getStatus());
    }

    @Test void shippersOnlySeeTheirAssignmentsAndCannotSkipDeliveryStages() {
        User owner = account(RoleName.VENDOR), first = account(RoleName.SHIPPER), second = account(RoleName.SHIPPER);
        Order order = order(owner,shop(owner)); order.setStatus(OrderStatus.ASSIGNED); orders.save(order);
        var assignment = new ShipperAssignment(); assignment.setOrder(order); assignment.setShipper(first);
        Long assignmentId = assignments.save(assignment).getId();
        assertEquals(1,shipperService.countAll(first.getId()));
        assertEquals(0,shipperService.countAll(second.getId()));
        assertEquals(0,shipperService.getAssignments(second.getId(),0,10).getTotalElements());
        assertThrows(IllegalArgumentException.class, () -> shipperService.getAssignment(assignmentId,second.getId()));
        assertThrows(IllegalArgumentException.class, () -> shipperService.updateOrderStatus(assignmentId,second.getId(),OrderStatus.PICKED_UP));
        assertThrows(IllegalArgumentException.class, () -> shipperService.updateOrderStatus(assignmentId,first.getId(),OrderStatus.DELIVERED));
        shipperService.updateOrderStatus(assignmentId,first.getId(),OrderStatus.PICKED_UP);
        shipperService.updateOrderStatus(assignmentId,first.getId(),OrderStatus.SHIPPING);
        shipperService.updateOrderStatus(assignmentId,first.getId(),OrderStatus.DELIVERED);
        assertEquals(1,shipperService.countByStatus(first.getId(),OrderStatus.DELIVERED));
        assertEquals(0,shipperService.countByStatus(second.getId(),OrderStatus.DELIVERED));
        assertEquals(PaymentStatus.PAID,orders.findById(order.getId()).orElseThrow().getPaymentStatus());
    }

    private User account(RoleName name) {
        var account = new User(); account.setEmail(UUID.randomUUID()+"@example.test"); account.setFullName("Role audit");
        account.setPassword("unused"); account.setStatus(UserStatus.ACTIVE);
        account.getRoles().add(roles.findByName(name).orElseGet(() -> {var role=new Role();role.setName(name);return roles.save(role);}));
        return users.save(account);
    }
    private Shop shop(User owner) {
        var shop = new Shop(); shop.setName("Role audit shop"); shop.setOwner(owner); shop.setStatus(ShopStatus.ACTIVE);
        return shops.save(shop);
    }
    private Order order(User buyer,Shop shop) {
        var order = new Order(); order.setUser(buyer); order.setShop(shop); order.setReceiverName("Buyer");
        order.setReceiverPhone("0900000000"); order.setShippingAddress("Audit address");
        order.setSubtotal(new BigDecimal("100000")); order.setTotalAmount(new BigDecimal("100000")); order.setPaymentMethod(PaymentMethod.COD);
        return orders.save(order);
    }
}
