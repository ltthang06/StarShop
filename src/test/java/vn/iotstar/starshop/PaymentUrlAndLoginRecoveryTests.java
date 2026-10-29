package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import vn.iotstar.starshop.config.VnpayProperties;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentUrlAndLoginRecoveryTests {
    @Autowired MockMvc mvc;

    @Test void staleLoginFormReturnsToFreshFormInsteadOfWhitelabel403() throws Exception {
        mvc.perform(post("/login").param("email","manager1@starshop.local").param("password","wrong"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?expired"));
    }

    @Test void automaticReturnUrlKeepsPublicHttpsAndLocalOrigins() {
        var config=new VnpayProperties();
        assertEquals("https://shop.example/payments/vnpay/return",config.resolveReturnUrl("https://shop.example/payments/vnpay/return"));
        assertEquals("http://localhost:8080/payments/vnpay/return",config.resolveReturnUrl("http://localhost:8080/payments/vnpay/return"));
        config.setReturnUrl("https://fixed.example/payments/vnpay/return");
        assertEquals(config.getReturnUrl(),config.resolveReturnUrl("http://localhost:8080/payments/vnpay/return"));
        config.setReturnUrl("");
        assertThrows(IllegalArgumentException.class,() -> config.resolveReturnUrl("javascript:alert(1)"));
    }
}
