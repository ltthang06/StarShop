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
    @Test void managementJspPagesRenderAfterLogin() throws Exception {
        Role role=roles.findByName(RoleName.MANAGER).orElseGet(() -> {Role r=new Role();r.setName(RoleName.MANAGER);return roles.save(r);});
        User manager=new User(); manager.setFullName("Page Test"); manager.setEmail(UUID.randomUUID()+"@example.test");
        manager.setPassword(encoder.encode("page-password")); manager.setStatus(UserStatus.ACTIVE); manager.getRoles().add(role);
        users.save(manager);
        var client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
        String root="http://localhost:"+port;
        String login=client.send(HttpRequest.newBuilder(URI.create(root+"/login")).GET().build(),HttpResponse.BodyHandlers.ofString()).body();
        var matcher=Pattern.compile("name=\\x22_csrf\\x22[^>]*value=\\x22([^\\x22]+)\\x22").matcher(login);
        assertTrue(matcher.find(), "Login CSRF field must exist");
        String body="username="+URLEncoder.encode(manager.getEmail(),StandardCharsets.UTF_8)
            +"&password=page-password&_csrf="+URLEncoder.encode(matcher.group(1),StandardCharsets.UTF_8);
        var response=client.send(HttpRequest.newBuilder(URI.create(root+"/login"))
            .header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),
            HttpResponse.BodyHandlers.ofString());
        assertEquals(302,response.statusCode());
        for(String page : new String[]{"/manager","/manager/users","/manager/shipping-providers","/manager/orders","/manager/shops","/manager/categories/new"}) {
            var result=client.send(HttpRequest.newBuilder(URI.create(root+page)).GET().build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(200,result.statusCode(),page);
            assertTrue(result.body().contains("StarShop"),page);
        }
    }
}
