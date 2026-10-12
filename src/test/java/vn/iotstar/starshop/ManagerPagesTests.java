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
    @Test void managementJspPagesRenderAfterLogin() throws Exception {
        Role role=roles.findByName(RoleName.MANAGER).orElseGet(() -> {Role r=new Role();r.setName(RoleName.MANAGER);return roles.save(r);});
        User manager=new User(); manager.setFullName("Page Test"); manager.setEmail(UUID.randomUUID()+"@example.test");
        manager.setPassword(encoder.encode("page-password")); manager.setStatus(UserStatus.ACTIVE); manager.getRoles().add(role);
        users.save(manager);
        Shop shop=new Shop();shop.setName("Page shop");shop.setOwner(manager);shop=shops.save(shop);
        Category category=new Category();category.setName("Page category");category=categories.save(category);
        Product product=new Product();product.setName("Page flower");product.setDescription("Page test product");
        product.setPrice(new java.math.BigDecimal("100000"));product.setQuantity(2);product.setShop(shop);product.setCategory(category);
        product=products.save(product);
        catalog.moderate(product.getId(),true,"Lý do kiểm duyệt sản phẩm",manager.getEmail());
        var promo=new vn.iotstar.starshop.dto.PromotionRequest();promo.setCode("PAGE"+UUID.randomUUID().toString().substring(0,8));
        promo.setName("Page voucher");promo.setType(PromotionType.FREE_SHIPPING);promo.setQuantity(10);
        promo.setStartAt(java.time.LocalDateTime.now().minusHours(1));promo.setEndAt(java.time.LocalDateTime.now().plusDays(1));
        Long promotionId=catalog.savePromotion(null,promo).getId();
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
                "/manager/users/"+manager.getId(),"/manager/shops/"+shop.getId(),"/manager/promotions/"+promotionId+"/edit"}) {
            var result=client.send(HttpRequest.newBuilder(URI.create(root+page)).GET().build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(200,result.statusCode(),page);
            assertTrue(result.body().contains("StarShop"),page);
            if(page.startsWith("/manager")) assertTrue(result.body().contains("manager-layout-footer"),"SiteMesh decorator: "+page);
        }
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
