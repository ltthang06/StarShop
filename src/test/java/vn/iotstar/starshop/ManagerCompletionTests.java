package vn.iotstar.starshop;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.dto.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.*;
@SpringBootTest
@org.springframework.security.test.context.support.WithMockUser(username="admin-test@example.test",roles="ADMIN")
class ManagerCompletionTests {
    @Autowired ManagerCatalogService catalog;
    @Autowired ManagerShopService management;
    @Autowired ManagerOperationsService operations;
    @Autowired ManagerReportService reports;
    @Autowired ProductService vendorProducts;
    @Autowired OrderService vendorOrders;
    @Autowired ShipperService delivery;
    @Autowired CustomerCartService cart;
    @Autowired CustomerCheckoutService checkout;
    @Autowired CustomerOrderService history;
    @Autowired CustomerReviewService reviews;
    @Autowired ManagerUserRepository users;
    @Autowired RoleRepository roles;
    @Autowired ManagerShopRepository shops;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;
    @Autowired OrderDetailRepository details;
    @Autowired CustomerAddressRepository addresses;
    @Autowired ShippingProviderRepository providers;
    @Autowired ShipperAssignmentRepository assignments;
    @Autowired NotificationService notifications;
    @Autowired AdminUserService admin;
    @Autowired PasswordEncoder encoder;

    @Test void customerVendorManagerShipperCustomerAndRefund() throws Exception {
        User customer=account(RoleName.USER),vendor=account(RoleName.USER),shipper=account(RoleName.SHIPPER);
        Shop shop=new Shop();shop.setName("Flow "+UUID.randomUUID());shop.setOwner(vendor);shop=shops.save(shop);
        management.approve(shop.getId()); management.setCommission(shop.getId(),new BigDecimal("5.00"));
        Category category=new Category();category.setName("Flowers "+UUID.randomUUID());category=categories.save(category);
        ProductRequest request=productForm(category.getId());
        Product product=vendorProducts.createProduct(shop.getId(),vendor.getId(),request);
        ShippingProviderForm shipping=new ShippingProviderForm();shipping.setName("Shipping "+UUID.randomUUID());
        shipping.setBaseFee(new BigDecimal("20000"));shipping.setEstimatedDays(2);operations.saveProvider(shipping);
        var provider=providers.findAllByOrderByNameAsc().stream().filter(p -> p.getName().equals(shipping.getName())).findFirst().orElseThrow();
        Address address=new Address();address.setUser(customer);address.setRecipientName("Customer");address.setPhone("0900123456");
        address.setAddressLine("123 Flower Street");address.setWard("Ward");address.setDistrict("District");address.setProvince("HCM");
        address=addresses.save(address);
        var voucher=catalog.savePromotion(null,promotionForm());
        cart.add(customer.getEmail(),product.getId(),2);
        Long orderId=checkout.placeOrder(customer.getEmail(),address.getId(),provider.getId(),"",voucher.getCode()).get(0);
        assertEquals(1,history.history(customer.getEmail(),OrderStatus.NEW,0).getTotalElements());
        assertEquals(0,history.history(vendor.getEmail(),OrderStatus.NEW,0).getTotalElements());
        var managerView=operations.orderDetail(orderId);
        assertEquals(customer.getEmail(),managerView.order().getUser().getEmail());
        assertEquals(1,managerView.lines().size());
        assertEquals(2,managerView.lines().get(0).quantity());
        assertEquals(0,new BigDecimal("20000").compareTo(orders.findById(orderId).orElseThrow().getDiscountAmount()));
        vendorOrders.confirmOrder(orderId,shop.getId(),vendor.getId());
        vendorOrders.markReadyForPickup(orderId,shop.getId(),vendor.getId());
        operations.assign(orderId,shipper.getId(),provider.getId(),"");
        assertEquals(shipper.getEmail(),operations.orderDetail(orderId).shipperEmail());
        Long assignmentId=assignments.findByOrder_Id(orderId).orElseThrow().getId();
        assertThrows(IllegalArgumentException.class,() -> delivery.updateOrderStatus(assignmentId,customer.getId(),OrderStatus.PICKED_UP));
        delivery.updateOrderStatus(assignmentId,shipper.getId(),OrderStatus.PICKED_UP);
        delivery.updateOrderStatus(assignmentId,shipper.getId(),OrderStatus.SHIPPING);
        delivery.updateOrderStatus(assignmentId,shipper.getId(),OrderStatus.DELIVERED);
        assertEquals(PaymentStatus.PAID,orders.findById(orderId).orElseThrow().getPaymentStatus());
        assertEquals(0,history.history(customer.getEmail(),OrderStatus.NEW,0).getTotalElements());
        assertEquals(1,history.history(customer.getEmail(),OrderStatus.DELIVERED,0).getTotalElements());
        assertEquals(OrderStatus.DELIVERED,history.history(customer.getEmail(),0).getContent().get(0).getStatus());
        Long detailId=details.findByOrderIdOrderByIdAsc(orderId).get(0).getId();
        reviews.submit(customer.getEmail(),orderId,detailId,5,"Hoa đẹp và đóng gói cẩn thận, giao hàng đúng thời gian. Tôi rất hài lòng với sản phẩm.",null,null);
        assertTrue(notifications.unread(shipper.getEmail())>0);
        assertTrue(reports.report(LocalDate.now(),LocalDate.now()).deliveredRevenue().signum()>0);
        history.requestReturn(customer.getEmail(),orderId);
        vendorOrders.markReturned(orderId,shop.getId(),vendor.getId());
        vendorOrders.refundOrder(orderId,shop.getId(),vendor.getId());
        assertEquals(OrderStatus.REFUNDED,history.history(customer.getEmail(),0).getContent().get(0).getStatus());
        assertEquals(10,products.findById(product.getId()).orElseThrow().getQuantity());
        assertEquals(0,products.findById(product.getId()).orElseThrow().getSoldCount());
        cart.add(customer.getEmail(),product.getId(),1);
        Long cancelId=checkout.placeOrder(customer.getEmail(),address.getId(),provider.getId(),"",null).get(0);
        vendorOrders.cancelOrder(cancelId,shop.getId(),vendor.getId());
        assertEquals(10,products.findById(product.getId()).orElseThrow().getQuantity());
        Long checkedShopId=shop.getId();
        assertThrows(IllegalArgumentException.class,() -> vendorOrders.cancelOrder(cancelId,checkedShopId,vendor.getId()));
    }
    @Test void moderationCannotBeBypassedByVendorAndVoucherScopeIsProtected() {
        User vendor=account(RoleName.VENDOR);
        Shop shop=new Shop();shop.setName("Moderation");shop.setOwner(vendor);shop.setStatus(ShopStatus.ACTIVE);shop=shops.save(shop);
        Category category=new Category();category.setName("Moderation "+UUID.randomUUID());category=categories.save(category);
        ProductRequest form=productForm(category.getId());
        Product p=vendorProducts.createProduct(shop.getId(),vendor.getId(),form);
        catalog.moderate(p.getId(),true,"Nội dung sản phẩm vi phạm quy định","manager@example.test");
        Long shopId=shop.getId();
        assertThrows(IllegalArgumentException.class,() -> vendorProducts.updateProduct(p.getId(),shopId,vendor.getId(),form));
        catalog.moderate(p.getId(),false,"","manager@example.test");
        assertEquals(ProductStatus.INACTIVE,products.findById(p.getId()).orElseThrow().getStatus());
        vendorProducts.updateProduct(p.getId(),shopId,vendor.getId(),form);
        assertEquals(ProductStatus.ACTIVE,products.findById(p.getId()).orElseThrow().getStatus());
        PromotionRequest promo=promotionForm();promo.setDiscountValue(new BigDecimal("101"));
        assertThrows(IllegalArgumentException.class,() -> catalog.savePromotion(null,promo));
        promo.setDiscountValue(new BigDecimal("10"));var saved=catalog.savePromotion(null,promo);
        assertEquals(PromotionScope.SYSTEM,saved.getScope());
        promo.setCode(saved.getCode().toLowerCase());
        assertThrows(IllegalArgumentException.class,() -> catalog.savePromotion(null,promo));
        assertThrows(IllegalArgumentException.class,() -> reports.report(LocalDate.now(),LocalDate.now().minusDays(1)));
        var report=reports.report(null,null);assertTrue(reports.csv(report).contains("DELIVERED"));
    }
    @Test void notificationsAndOperationalRolesAreOwned() {
        User first=account(RoleName.USER),second=account(RoleName.USER);
        notifications.send(first,"Test","Private","/orders");
        var notification=notifications.list(first.getEmail(),0).getContent().get(0);
        assertThrows(IllegalArgumentException.class,() -> notifications.markRead(notification.getId(),second.getEmail()));
        notifications.markRead(notification.getId(),first.getEmail());assertEquals(0,notifications.unread(first.getEmail()));
        admin.setOperationalRole(first.getId(),RoleName.SHIPPER,true,"admin@example.test");
        assertTrue(users.findById(first.getId()).orElseThrow().getRoles().stream().anyMatch(r -> r.getName()==RoleName.SHIPPER));
        assertThrows(IllegalArgumentException.class,() -> admin.setOperationalRole(first.getId(),RoleName.ADMIN,true,"admin@example.test"));
    }
    private ProductRequest productForm(Long categoryId) {
        var p=new ProductRequest();p.setCategoryId(categoryId);p.setName("Flower");p.setDescription("Flower description");
        p.setPrice(new BigDecimal("100000"));p.setQuantity(10);return p;
    }
    private PromotionRequest promotionForm() {
        var p=new PromotionRequest();p.setCode("SYS"+UUID.randomUUID().toString().substring(0,8));p.setName("System 10%");
        p.setType(PromotionType.PERCENT);p.setDiscountValue(new BigDecimal("10"));p.setQuantity(10);
        p.setStartAt(LocalDateTime.now().minusHours(1));p.setEndAt(LocalDateTime.now().plusDays(1));return p;
    }
    private User account(RoleName name) {
        Role role=roles.findByName(name).orElseGet(() -> {Role r=new Role();r.setName(name);return roles.save(r);});
        User u=new User();u.setEmail(UUID.randomUUID()+"@example.test");u.setFullName("Integration");
        u.setPassword(encoder.encode("test-password"));u.setStatus(UserStatus.ACTIVE);u.getRoles().add(role);return users.save(u);
    }
}
