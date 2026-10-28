package vn.iotstar.starshop;
import static org.junit.jupiter.api.Assertions.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.security.test.context.support.WithMockUser(username="admin-test@example.test",roles="ADMIN")
class ManagerPagesTests {
    @LocalServerPort int port;
    @Autowired SecurityUserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder encoder;
    @Autowired vn.iotstar.starshop.service.NotificationService notifications;
    @Autowired vn.iotstar.starshop.service.ManagerCatalogService catalog;
    @Autowired ManagerShopRepository shops;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;
    @Autowired OrderDetailRepository orderDetails;
    @Autowired vn.iotstar.starshop.service.ManagerShopService shopService;

    @Test void branchManagerSeesOnlyBranchMenusAndReadOnlyCommissionAndCannotPostAdminActions() throws Exception {
        Role role=roles.findByName(RoleName.MANAGER).orElseGet(() -> {var r=new Role();r.setName(RoleName.MANAGER);return roles.save(r);});
        var manager=new User();manager.setFullName("Branch page manager");manager.setEmail(UUID.randomUUID()+"@example.test");
        manager.setPassword(encoder.encode("branch-page-password"));manager.setStatus(UserStatus.ACTIVE);manager.getRoles().add(role);
        manager=users.save(manager);
        var otherManager=new User();otherManager.setFullName("Other branch private manager");otherManager.setEmail(UUID.randomUUID()+"@example.test");
        otherManager.setPhone("0912"+String.format("%06d",java.util.concurrent.ThreadLocalRandom.current().nextInt(1000000)));
        otherManager.setPassword(encoder.encode("other-branch-password"));otherManager.setStatus(UserStatus.ACTIVE);otherManager.getRoles().add(role);
        otherManager=users.save(otherManager);
        var own=new Shop();own.setName("Assigned branch page shop");own.setOwner(manager);own.setManager(manager);own.setStatus(ShopStatus.ACTIVE);own=shops.save(own);
        var other=new Shop();other.setName("Other branch page shop");other.setOwner(otherManager);other.setManager(otherManager);other.setStatus(ShopStatus.ACTIVE);other=shops.save(other);
        shopService.setCommission(own.getId(),new java.math.BigDecimal("5.00"));
        var client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
        String root="http://localhost:"+port;
        String login=client.send(HttpRequest.newBuilder(URI.create(root+"/login")).GET().build(),HttpResponse.BodyHandlers.ofString()).body();
        var csrf=Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(login);
        assertTrue(csrf.find());
        String body="email="+URLEncoder.encode(manager.getEmail(),StandardCharsets.UTF_8)
                +"&password=branch-page-password&_csrf="+URLEncoder.encode(csrf.group(1),StandardCharsets.UTF_8);
        assertEquals(302,client.send(HttpRequest.newBuilder(URI.create(root+"/login"))
                .header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString()).statusCode());
        var detail=client.send(HttpRequest.newBuilder(URI.create(root+"/manager/shops/"+own.getId())).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,detail.statusCode());
        assertTrue(detail.body().contains("StarShop Manager"));
        assertTrue(detail.body().contains("Tỷ lệ chiết khấu do Admin thiết lập."));
        assertFalse(detail.body().contains("name=\"ratePercent\""));
        assertFalse(detail.body().contains("/manager/shops/"+own.getId()+"/block"));
        for(String path:new String[]{"users","categories","promotions","shipping-providers","payments"}) {
            assertFalse(detail.body().contains("href=\"/manager/"+path+"\""),path);
            assertEquals(403,client.send(HttpRequest.newBuilder(URI.create(root+"/manager/"+path)).GET().build(),HttpResponse.BodyHandlers.ofString()).statusCode(),path);
        }
        for(String path:new String[]{"/manager/users/"+otherManager.getId(),"/manager/users?keyword="+URLEncoder.encode(otherManager.getEmail(),StandardCharsets.UTF_8),"/manager/shops/"+other.getId()}) {
            var denied=client.send(HttpRequest.newBuilder(URI.create(root+path)).GET().build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(403,denied.statusCode(),path);
            assertFalse(denied.body().contains(otherManager.getFullName()));
            assertFalse(denied.body().contains(otherManager.getEmail()));
            assertFalse(denied.body().contains(otherManager.getPhone()));
        }
        var token=Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(detail.body());
        assertTrue(token.find());
        String fields="ratePercent=1&managerId="+manager.getId()+"&_csrf="+URLEncoder.encode(token.group(1),StandardCharsets.UTF_8);
        for(String action:new String[]{"commission","manager","approve","reject","block","reopen"}) {
            var result=client.send(HttpRequest.newBuilder(URI.create(root+"/manager/shops/"+own.getId()+"/"+action))
                    .header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(fields)).build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(403,result.statusCode(),action);
        }
        for(String path:new String[]{"/manager/categories/save","/manager/promotions/save","/manager/shipping-providers","/manager/users/"+manager.getId()+"/lock"}) {
            assertEquals(403,client.send(HttpRequest.newBuilder(URI.create(root+path))
                    .header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(fields)).build(),HttpResponse.BodyHandlers.ofString()).statusCode(),path);
        }
        assertEquals(0,new java.math.BigDecimal("5").compareTo(shopService.currentCommission(own.getId()).orElseThrow().getRatePercent()));
        var dashboard=client.send(HttpRequest.newBuilder(URI.create(root+"/manager")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,dashboard.statusCode());assertTrue(dashboard.body().contains("Quản lý chi nhánh"));
        assertFalse(dashboard.body().contains("Shop chờ duyệt"));
        var report=client.send(HttpRequest.newBuilder(URI.create(root+"/manager/reports")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,report.statusCode());assertTrue(report.body().contains("Báo cáo chi nhánh"));
        assertFalse(report.body().contains("Người dùng toàn hệ thống"));
        assertFalse(report.body().contains(otherManager.getEmail()));
        var branchList=client.send(HttpRequest.newBuilder(URI.create(root+"/manager/shops")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,branchList.statusCode());assertFalse(branchList.body().contains(otherManager.getEmail()));
        var profile=client.send(HttpRequest.newBuilder(URI.create(root+"/profile")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,profile.statusCode());assertTrue(profile.body().contains(manager.getEmail()));
        assertFalse(profile.body().contains(otherManager.getEmail()));
    }

    @Test void adminJspPagesRenderAfterLogin() throws Exception {
        Role role=roles.findByName(RoleName.ADMIN).orElseGet(() -> {Role r=new Role();r.setName(RoleName.ADMIN);return roles.save(r);});
        User manager=new User(); manager.setFullName("Page Test"); manager.setEmail(UUID.randomUUID()+"@example.test");
        manager.setPassword(encoder.encode("page-password")); manager.setStatus(UserStatus.ACTIVE); manager.getRoles().add(role);
        users.save(manager);
        Shop shop=new Shop();shop.setName("Page shop");shop.setOwner(manager);shop.setManager(manager);shop=shops.save(shop);
        Category category=new Category();category.setName("Page category");category=categories.save(category);
        Product product=new Product();product.setName("Page flower");product.setDescription("Page test product");
        product.setPrice(new java.math.BigDecimal("100000"));product.setQuantity(2);product.setShop(shop);product.setCategory(category);
        product=products.save(product);
        catalog.moderate(product.getId(),true,"Lý do kiểm duyệt sản phẩm",manager.getEmail());
        var promo=new vn.iotstar.starshop.dto.PromotionRequest();promo.setCode("PAGE"+UUID.randomUUID().toString().substring(0,8));
        promo.setName("Page voucher");promo.setType(PromotionType.FREE_SHIPPING);promo.setQuantity(10);
        promo.setStartAt(java.time.LocalDateTime.now().minusHours(1));promo.setEndAt(java.time.LocalDateTime.now().plusDays(1));
        Long promotionId=catalog.savePromotion(null,promo).getId();
        Order order=new Order();order.setUser(manager);order.setShop(shop);
        order.setReceiverName("Page buyer");order.setReceiverPhone("0900000000");order.setShippingAddress("Page address");
        order.setSubtotal(new java.math.BigDecimal("100000"));order.setTotalAmount(new java.math.BigDecimal("100000"));
        order.setPaymentMethod(PaymentMethod.COD);order=orders.save(order);
        OrderDetail line=new OrderDetail();line.setOrder(order);line.setProduct(product);line.setQuantity(1);
        line.setUnitPrice(new java.math.BigDecimal("100000"));line.setSubtotal(new java.math.BigDecimal("100000"));orderDetails.save(line);
        var client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
        String root="http://localhost:"+port;
        String login=client.send(HttpRequest.newBuilder(URI.create(root+"/login")).GET().build(),HttpResponse.BodyHandlers.ofString()).body();
        var matcher=Pattern.compile("name=\\x22_csrf\\x22[^>]*value=\\x22([^\\x22]+)\\x22").matcher(login);
        assertTrue(matcher.find(), "Login CSRF field must exist");
        String body="email="+URLEncoder.encode(manager.getEmail(),StandardCharsets.UTF_8)
            +"&password=page-password&_csrf="+URLEncoder.encode(matcher.group(1),StandardCharsets.UTF_8);
        var response=client.send(HttpRequest.newBuilder(URI.create(root+"/login"))
            .header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),
            HttpResponse.BodyHandlers.ofString());
        assertEquals(302,response.statusCode());
        for(String page : new String[]{"/manager","/manager/users","/manager/shipping-providers","/manager/orders","/manager/shops","/manager/categories/new","/manager/products","/manager/promotions","/manager/promotions/new","/manager/reports","/notifications",
                "/manager/users/"+manager.getId(),"/manager/shops/"+shop.getId(),"/manager/promotions/"+promotionId+"/edit",
                "/manager/orders/"+order.getId(),"/manager/products/"+product.getId()}) {
            var result=client.send(HttpRequest.newBuilder(URI.create(root+page)).GET().build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(200,result.statusCode(),page);
            assertTrue(result.body().contains("StarShop"),page);
            if(page.startsWith("/manager")) assertTrue(result.body().contains("manager-layout-footer"),"SiteMesh decorator: "+page);
            if(page.equals("/manager/orders/"+order.getId())) {
                assertTrue(result.body().contains("100.000 ₫"),"Money format on order detail");
                assertTrue(result.body().contains("Page flower"),"Order line product");
            }
        }
        for(String missing: new String[]{"/manager/orders/9223372036854775807","/manager/products/9223372036854775807",
                "/manager/categories/9223372036854775807/edit","/manager/shipping-providers?edit=9223372036854775807"}) {
            assertEquals(404,client.send(HttpRequest.newBuilder(URI.create(root+missing)).GET().build(),HttpResponse.BodyHandlers.ofString()).statusCode());
        }
        var invalidFilter=client.send(HttpRequest.newBuilder(URI.create(root+"/manager/orders?status=UNKNOWN")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(400,invalidFilter.statusCode());
        assertTrue(invalidFilter.body().contains("Thông số tìm kiếm không hợp lệ"));
        var received=new java.util.concurrent.CountDownLatch(1);
        var ready=new java.util.concurrent.CountDownLatch(1);
        var socket=client.newWebSocketBuilder().buildAsync(URI.create("ws://localhost:"+port+"/ws/notifications"),
            new WebSocket.Listener() {
                @Override public void onOpen(WebSocket webSocket) {webSocket.request(1);}
                @Override public java.util.concurrent.CompletionStage<?> onText(WebSocket webSocket,CharSequence data,boolean last) {
                    if(data.toString().contains("notification")) received.countDown();
                    if(data.toString().contains("ready")) ready.countDown();
                    webSocket.request(1);return null;
                }
            }).get(10,java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(ready.await(10,java.util.concurrent.TimeUnit.SECONDS),"WebSocket must be ready");
        notifications.send(manager,"Thông báo test","Nội dung test","/manager");
        assertTrue(received.await(10,java.util.concurrent.TimeUnit.SECONDS),"Authenticated WebSocket must receive notification");
        socket.sendClose(WebSocket.NORMAL_CLOSURE,"test complete").join();
        var crossOrigin=assertThrows(java.util.concurrent.ExecutionException.class,() ->
            client.newWebSocketBuilder().header("Origin","https://untrusted.example")
                .buildAsync(URI.create("ws://localhost:"+port+"/ws/notifications"),new WebSocket.Listener() {})
                .get(10,java.util.concurrent.TimeUnit.SECONDS));
        assertTrue(crossOrigin.getCause() instanceof WebSocketHandshakeException);
        assertEquals(403,((WebSocketHandshakeException)crossOrigin.getCause()).getResponse().statusCode());
    }
}
