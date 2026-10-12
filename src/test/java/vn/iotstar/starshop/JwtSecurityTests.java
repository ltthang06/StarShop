package vn.iotstar.starshop;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.JwtTokenService;
@SpringBootTest(properties="starshop.sitemesh.enabled=false") @AutoConfigureMockMvc
class JwtSecurityTests {
    @Autowired MockMvc mvc;
    @Autowired JwtTokenService tokens;
    @Autowired ManagerUserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder encoder;
    @Autowired org.springframework.security.oauth2.jwt.JwtEncoder jwtEncoder;
    @Test void bearerRolesAndAccountLockAreEnforced() throws Exception {
        User customer=account(RoleName.USER),manager=account(RoleName.MANAGER);
        String customerToken=(String)tokens.issue(customer.getEmail(),"password").get("access_token");
        String managerToken=(String)tokens.issue(manager.getEmail(),"password").get("access_token");
        mvc.perform(get("/api/me").header("Authorization","Bearer "+customerToken)).andExpect(status().isOk());
        mvc.perform(get("/api/manager/report").header("Authorization","Bearer "+customerToken)).andExpect(status().isForbidden());
        mvc.perform(get("/api/manager/report").header("Authorization","Bearer "+managerToken)).andExpect(status().isOk());
        mvc.perform(get("/api/me").header("Authorization","Bearer "+managerToken+"x")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        manager.setStatus(UserStatus.LOCKED);users.save(manager);
        mvc.perform(get("/api/me").header("Authorization","Bearer "+managerToken)).andExpect(status().isUnauthorized());
        mvc.perform(post("/manager/users/{id}/roles",customer.getId()).param("role","SHIPPER").param("enabled","true")
            .with(user("manager@example.test").roles("MANAGER")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/manager/promotions/save").with(user("manager@example.test").roles("MANAGER")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/ws/notifications").with(user(customer.getEmail()).roles("USER")))
            .andExpect(status().isBadRequest());
    }
    @Test void tokenEndpointAndPasswordChangeInvalidateOldToken() throws Exception {
        User user=account(RoleName.USER);
        mvc.perform(post("/api/auth/token").contentType("application/json")
            .content("{\"email\":\""+user.getEmail()+"\",\"password\":\"password\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.token_type").value("Bearer"));
        mvc.perform(post("/api/auth/token").contentType("application/json").content("{\"email\":\""+user.getEmail()+"\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
        String token=(String)tokens.issue(user.getEmail(),"password").get("access_token");
        user.setPassword(encoder.encode("new-password"));users.save(user);
        mvc.perform(get("/api/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
    }
    @Test void expiredAndForeignIssuerTokensAreRejected() throws Exception {
        User user=account(RoleName.USER);
        for (String issuer:new String[]{"starshop","other-app"}) {
            var now=java.time.Instant.now();
            var claims=org.springframework.security.oauth2.jwt.JwtClaimsSet.builder().issuer(issuer).subject(user.getEmail())
                .issuedAt(now.minusSeconds(300)).expiresAt(issuer.equals("starshop") ? now.minusSeconds(120) : now.plusSeconds(900))
                .claim("credential",JwtTokenService.fingerprint(user.getPassword())).build();
            String token=jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build(),claims)).getTokenValue();
            mvc.perform(get("/api/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        }
    }
    @Test void webSessionCannotKeepRevokedManagerRole() throws Exception {
        User manager=account(RoleName.MANAGER);
        manager.getRoles().clear();users.save(manager);
        mvc.perform(get("/manager").with(user(manager.getEmail()).roles("MANAGER")))
            .andExpect(status().isForbidden());
    }
    private User account(RoleName name) {
        Role role=roles.findByName(name).orElseGet(() -> {Role r=new Role();r.setName(name);return roles.save(r);});
        User u=new User();u.setEmail(UUID.randomUUID()+"@example.test");u.setFullName("JWT");
        u.setPassword(encoder.encode("password"));u.setStatus(UserStatus.ACTIVE);u.getRoles().add(role);return users.save(u);
    }
}
