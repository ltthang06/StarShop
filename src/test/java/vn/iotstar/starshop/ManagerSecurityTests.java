package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import vn.iotstar.starshop.entity.Role;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.RoleName;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.RoleRepository;
import vn.iotstar.starshop.repository.ManagerShopRepository;
import vn.iotstar.starshop.repository.SecurityUserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ManagerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private SecurityUserRepository userRepository;
    @Autowired private ManagerShopRepository shopRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void ordinaryUserCannotOpenManagement() throws Exception {
        mockMvc.perform(get("/manager").with(user("customer@example.com").roles("USER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/manager/shops").with(user("customer@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanOpenManagement() throws Exception {
        mockMvc.perform(get("/manager/shops").with(user("manager@example.com").roles("MANAGER")))
                .andExpect(status().isOk());
    }

    @Test
    void activeManagerCanSignInWithDatabaseRole() throws Exception {
        Role managerRole = roleRepository.findByName(RoleName.MANAGER).orElseGet(() -> {
            Role role = new Role();
            role.setName(RoleName.MANAGER);
            return roleRepository.save(role);
        });
        User manager = new User();
        manager.setFullName("Manager Test");
        manager.setEmail(UUID.randomUUID() + "@example.test");
        manager.setPassword(passwordEncoder.encode("test-password"));
        manager.setStatus(UserStatus.ACTIVE);
        manager.getRoles().add(managerRole);
        userRepository.save(manager);

        mockMvc.perform(formLogin().user(manager.getEmail()).password("test-password"))
                .andExpect(authenticated().withRoles("MANAGER"));
    }

    @Test
    void ordinaryUserCannotApproveShop() throws Exception {
        User owner = new User();
        owner.setFullName("Vendor Test");
        owner.setEmail(UUID.randomUUID() + "@example.test");
        owner.setPassword("unused-test-password");
        owner.setStatus(UserStatus.ACTIVE);
        owner = userRepository.save(owner);
        Shop shop = new Shop();
        shop.setName("Shop Security Test");
        shop.setOwner(owner);
        shop = shopRepository.save(shop);

        mockMvc.perform(post("/manager/shops/{id}/approve", shop.getId())
                .with(user("customer@example.com").roles("USER")).with(csrf()))
                .andExpect(status().isForbidden());
        assertEquals(ShopStatus.PENDING,
                shopRepository.findById(shop.getId()).orElseThrow().getStatus());
    }
}
