package vn.iotstar.starshop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import vn.iotstar.starshop.entity.OtpToken;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.OtpTokenRepository;
import vn.iotstar.starshop.repository.SecurityUserRepository;
import vn.iotstar.starshop.service.EmailOtpService;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerAuthFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityUserRepository userRepository;

    @Autowired
    private OtpTokenRepository otpRepository;

    @MockBean
    private EmailOtpService emailOtpService;

    @Test
    void customerCanRegisterActivateAndLogIn() throws Exception {
        String email = "customer@example.com";

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("fullName", "Khách Hàng")
                .param("email", email)
                .param("password", "matkhau123")
                .param("confirmPassword", "matkhau123"))
                .andExpect(status().is3xxRedirection());

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);

        OtpToken token = otpRepository
                .findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(
                        user.getId())
                .orElseThrow();

        mockMvc.perform(post("/verify-otp")
                .with(csrf())
                .param("email", email)
                .param("code", token.getCode()))
                .andExpect(status().is3xxRedirection());

        assertThat(userRepository.findByEmailIgnoreCase(email)
                .orElseThrow()
                .getStatus()).isEqualTo(UserStatus.ACTIVE);

        mockMvc.perform(post("/login")
                .with(csrf())
                .param("email", email)
                .param("password", "matkhau123"))
                .andExpect(authenticated().withUsername(email));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/products"));
    }
}
