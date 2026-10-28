package vn.iotstar.starshop;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.*;
@SpringBootTest
class ManagerOperationsTests {
    @Autowired ManagerOperationsService service;
    @Autowired ShipperService shipperService;
    @Autowired ManagerUserRepository users;
    @Autowired RoleRepository roles;
    @Autowired ManagerShopRepository shops;
    @Autowired ManagerOrderRepository orders;
    @Autowired ShippingProviderRepository providers;
    @Autowired ShipperAssignmentRepository assignments;

    @Test void assignmentContinuesIntoExistingShipperFlow() {
        User owner = account(RoleName.USER);
        User shipper = account(RoleName.SHIPPER);
        Shop shop = new Shop(); shop.setName("Integration Shop"); shop.setOwner(owner);
        shop = shops.save(shop);
        ShippingProvider provider = new ShippingProvider();
        provider.setName(UUID.randomUUID().toString()); provider.setBaseFee(new BigDecimal("20000"));
        provider.setEstimatedDays(2); provider = providers.save(provider);
        Order order = new Order(); order.setShop(shop); order.setUser(owner);
        order.setReceiverName("Customer"); order.setReceiverPhone("0900000000");
        order.setShippingAddress("Test address"); order.setSubtotal(new BigDecimal("100000"));
        order.setTotalAmount(new BigDecimal("120000")); order.setShippingFee(new BigDecimal("20000"));
        order.setPaymentMethod(PaymentMethod.COD); order.setStatus(OrderStatus.READY_FOR_PICKUP);
        order = orders.save(order);
        Long orderId = order.getId(), shipperId = shipper.getId(), providerId = provider.getId();
        service.assign(orderId,shipperId,providerId,"Test note");
        assertEquals(OrderStatus.ASSIGNED, orders.findById(orderId).orElseThrow().getStatus());
        assertThrows(IllegalArgumentException.class, () -> service.assign(orderId,shipperId,providerId,""));
        var assignment = assignments.findByOrder_Id(orderId).orElseThrow();
        assertEquals(1, shipperService.getAssignments(shipperId,0,10).getTotalElements());
        shipperService.updateOrderStatus(assignment.getId(),shipperId,OrderStatus.PICKED_UP);
        shipperService.updateOrderStatus(assignment.getId(),shipperId,OrderStatus.SHIPPING);
        shipperService.updateOrderStatus(assignment.getId(),shipperId,OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED,orders.findById(orderId).orElseThrow().getStatus());
        assertEquals(new BigDecimal("120000.00"),orders.findById(orderId).orElseThrow().getTotalAmount());
    }
    @Test void lockDoesNotActivateUnverifiedAccountsOrAllowSelfLock() {
        User user = account(RoleName.USER);
        assertThrows(IllegalArgumentException.class, () -> service.setUserLocked(user.getId(),true,user.getEmail(),true));
        service.setUserLocked(user.getId(),true,"manager@example.test",false);
        assertEquals(UserStatus.LOCKED,users.findById(user.getId()).orElseThrow().getStatus());
        service.setUserLocked(user.getId(),false,"manager@example.test",false);
        user.setStatus(UserStatus.INACTIVE); users.save(user);
        assertThrows(IllegalArgumentException.class, () -> service.setUserLocked(user.getId(),false,"manager@example.test",true));
        User manager = account(RoleName.MANAGER);
        assertThrows(IllegalArgumentException.class, () -> service.setUserLocked(manager.getId(),true,"other@example.test",false));
    }
    private User account(RoleName name) {
        Role role = roles.findByName(name).orElseGet(() -> {Role r=new Role();r.setName(name);return roles.save(r);});
        User user = new User(); user.setEmail(UUID.randomUUID()+"@example.test"); user.setFullName("Test");
        user.setPassword("unused"); user.setStatus(UserStatus.ACTIVE); user.getRoles().add(role);
        return users.save(user);
    }
}
