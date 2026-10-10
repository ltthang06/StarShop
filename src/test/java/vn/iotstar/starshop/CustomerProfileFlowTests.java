package vn.iotstar.starshop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import vn.iotstar.starshop.entity.Address;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.CustomerAddressRepository;
import vn.iotstar.starshop.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerProfileFlowTests {

    private static final String EMAIL = "profile@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerAddressRepository addressRepository;

    @BeforeEach
    void createCustomer() {
        User user = new User();
        user.setFullName("Khách hàng");
        user.setEmail(EMAIL);
        user.setPassword("unused");
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void customerCanManageDefaultAddress() throws Exception {
        mockMvc.perform(post("/profile/addresses")
                .with(csrf())
                .param("recipientName", "Người nhận một")
                .param("phone", "0901234567")
                .param("addressLine", "Số 1"))
                .andExpect(redirectedUrl("/profile"));

        mockMvc.perform(post("/profile/addresses")
                .with(csrf())
                .param("recipientName", "Người nhận hai")
                .param("phone", "0907654321")
                .param("addressLine", "Số 2"))
                .andExpect(redirectedUrl("/profile"));

        Long userId = userRepository.findByEmail(EMAIL).orElseThrow().getId();
        List<Address> addresses = addressRepository
                .findByUserIdOrderByDefaultAddressDescIdAsc(userId);

        assertThat(addresses).hasSize(2);
        assertThat(addresses.stream()
                .filter(Address::isDefaultAddress)).hasSize(1);

        Address second = addresses.stream()
                .filter(address -> !address.isDefaultAddress())
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/profile/addresses/"
                + second.getId() + "/default").with(csrf()))
                .andExpect(redirectedUrl("/profile"));

        assertThat(addressRepository.findById(second.getId())
                .orElseThrow().isDefaultAddress()).isTrue();
    }
}
