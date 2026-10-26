package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.*;

@SpringBootTest
class ReviewConcurrencyTests {
    @Autowired UserRepository users;
    @Autowired ShopRepository shops;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;
    @Autowired OrderDetailRepository details;
    @Autowired CustomerReviewRepository reviews;
    @Autowired CustomerReviewService service;
    @Autowired CustomerOrderService customerOrders;
    @Autowired ProductModerationRepository moderations;

    private static final String TEXT="Hoa tươi, đóng gói cẩn thận, giao đúng hẹn và nhân viên hỗ trợ nhiệt tình.";

    @Test void twoBuyersReviewOneProductWithoutLosingAverageOrInventory() throws Exception {
        var first=fixture();
        var buyer=user();
        var second=line(buyer,first.product(),OrderStatus.DELIVERED);
        parallel(() -> {service.submit(first.user().getEmail(),first.line().getOrder().getId(),first.line().getId(),1,TEXT,null,null);return true;},
                () -> {service.submit(buyer.getEmail(),second.getOrder().getId(),second.getId(),5,TEXT,null,null);return true;});
        assertTrue(reviews.existsByOrderDetailId(first.line().getId()));
        assertTrue(reviews.existsByOrderDetailId(second.getId()));
        var product=products.findById(first.product().getId()).orElseThrow();
        assertEquals(3.0,product.getRating(),0.00001);
        assertEquals(8,product.getQuantity());
        assertEquals(2,product.getSoldCount());
    }

    @Test void doubleSubmitHasOneReviewAndControlledValidationError() throws Exception {
        var f=fixture();
        Callable<Boolean> action=() -> {
            try {service.submit(f.user().getEmail(),f.line().getOrder().getId(),f.line().getId(),4,TEXT,null,null);return true;}
            catch(IllegalArgumentException ex) {assertEquals("Sản phẩm này đã được đánh giá",ex.getMessage());return false;}
        };
        var results=parallel(action,action);
        assertEquals(1,results.stream().filter(Boolean::booleanValue).count());
        assertEquals(4.0,products.findById(f.product().getId()).orElseThrow().getRating());
    }

    @Test void cancellingTwiceRestoresInventoryOnceAndDoesNotPublishModeratedProduct() {
        var f=fixture();
        var product=f.product();product.setStatus(ProductStatus.OUT_OF_STOCK);product.setQuantity(0);products.save(product);
        var moderation=new ProductModeration();moderation.setProductId(product.getId());moderation.setBlocked(true);moderation.setReason("Blocked product for cancellation test");moderation.setModeratorEmail("admin@example.test");moderation.setUpdatedAt(java.time.LocalDateTime.now());moderations.save(moderation);
        var order=f.line().getOrder();order.setStatus(OrderStatus.NEW);orders.save(order);
        customerOrders.cancel(f.user().getEmail(),order.getId());
        assertThrows(IllegalArgumentException.class,() -> customerOrders.cancel(f.user().getEmail(),order.getId()));
        var saved=products.findById(product.getId()).orElseThrow();
        assertEquals(1,saved.getQuantity());assertEquals(1,saved.getSoldCount());
        assertNotEquals(ProductStatus.ACTIVE,saved.getStatus());
    }

    @Test void concurrentCancellationsOfDifferentOrdersRestoreBothQuantities() throws Exception {
        var f=fixture();
        var first=f.line().getOrder();first.setStatus(OrderStatus.NEW);orders.save(first);
        var second=line(f.user(),f.product(),OrderStatus.NEW);
        parallel(() -> {customerOrders.cancel(f.user().getEmail(),first.getId());return true;},
                () -> {customerOrders.cancel(f.user().getEmail(),second.getOrder().getId());return true;});
        var saved=products.findById(f.product().getId()).orElseThrow();
        assertEquals(10,saved.getQuantity());assertEquals(0,saved.getSoldCount());
        assertEquals(OrderStatus.CANCELLED,orders.findById(first.getId()).orElseThrow().getStatus());
        assertEquals(OrderStatus.CANCELLED,orders.findById(second.getOrder().getId()).orElseThrow().getStatus());
    }

    private List<Boolean> parallel(Callable<Boolean> first,Callable<Boolean> second) throws Exception {
        var ready=new CountDownLatch(2);var start=new CountDownLatch(1);
        var pool=Executors.newFixedThreadPool(2);
        try {
            var futures=new ArrayList<Future<Boolean>>();
            for(var action:List.of(first,second)) futures.add(pool.submit(() -> {
                ready.countDown();assertTrue(start.await(10,TimeUnit.SECONDS));return action.call();
            }));
            assertTrue(ready.await(10,TimeUnit.SECONDS));start.countDown();
            return List.of(futures.get(0).get(20,TimeUnit.SECONDS),futures.get(1).get(20,TimeUnit.SECONDS));
        } finally {start.countDown();pool.shutdownNow();}
    }
    private record Fixture(User user,Product product,OrderDetail line) {}
    private Fixture fixture() {
        var owner=user();var buyer=user();
        var shop=new Shop();shop.setName("Review shop");shop.setOwner(owner);shop.setStatus(ShopStatus.ACTIVE);shop=shops.save(shop);
        var category=new Category();category.setName("Review "+UUID.randomUUID());category=categories.save(category);
        var p=new Product();p.setName("Review flower");p.setDescription("Review test");p.setPrice(BigDecimal.valueOf(100000));p.setQuantity(8);p.setSoldCount(2);p.setShop(shop);p.setCategory(category);p=products.save(p);
        return new Fixture(buyer,p,line(buyer,p,OrderStatus.DELIVERED));
    }
    private User user() {
        var u=new User();u.setEmail(UUID.randomUUID()+"@example.test");u.setFullName("Review buyer");u.setPassword("unused");u.setStatus(UserStatus.ACTIVE);return users.save(u);
    }
    private OrderDetail line(User buyer,Product product,OrderStatus status) {
        var o=new vn.iotstar.starshop.entity.Order();o.setUser(buyer);o.setShop(product.getShop());o.setReceiverName("Buyer");o.setReceiverPhone("0900000000");o.setShippingAddress("Test address");o.setSubtotal(product.getPrice());o.setTotalAmount(product.getPrice());o.setPaymentMethod(PaymentMethod.COD);o.setStatus(status);o=orders.save(o);
        var line=new OrderDetail();line.setOrder(o);line.setProduct(product);line.setQuantity(1);line.setUnitPrice(product.getPrice());line.setSubtotal(product.getPrice());return details.save(line);
    }
}
