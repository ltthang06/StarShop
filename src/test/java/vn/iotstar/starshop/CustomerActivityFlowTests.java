package vn.iotstar.starshop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.CategoryRepository;
import vn.iotstar.starshop.repository.CustomerCatalogRepository;
import vn.iotstar.starshop.repository.CustomerProductViewRepository;
import vn.iotstar.starshop.repository.CustomerWishlistRepository;
import vn.iotstar.starshop.repository.ShopRepository;
import vn.iotstar.starshop.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerActivityFlowTests {

    private static final String EMAIL = "activity@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerCatalogRepository catalogRepository;

    @Autowired
    private CustomerWishlistRepository wishlistRepository;

    @Autowired
    private CustomerProductViewRepository viewRepository;

    private Long productId;
    private Long userId;

    @BeforeEach
    void createProduct() {
        User customer = new User();
        customer.setFullName("Khách hàng");
        customer.setEmail(EMAIL);
        customer.setPhone("0900000001");
        customer.setPassword("unused");
        customer.setStatus(UserStatus.ACTIVE);
        userId = userRepository.save(customer).getId();

        User vendor = new User();
        vendor.setFullName("Chủ shop");
        vendor.setEmail("vendor-activity@example.com");
        vendor.setPhone("0900000002");
        vendor.setPassword("unused");
        vendor.setStatus(UserStatus.ACTIVE);
        vendor = userRepository.save(vendor);

        Shop shop = new Shop();
        shop.setName("Tiệm hoa");
        shop.setOwner(vendor);
        shop.setStatus(ShopStatus.ACTIVE);
        shop = shopRepository.save(shop);

        Category category = new Category();
        category.setName("Hoa hồng kiểm tra");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Hoa hồng đỏ");
        product.setDescription("Bó hoa hồng đỏ");
        product.setPrice(BigDecimal.valueOf(120000));
        product.setQuantity(5);
        product.setShop(shop);
        product.setCategory(category);
        productId = catalogRepository.save(product).getId();
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void viewsAndWishlistCanBeManaged() throws Exception {
        mockMvc.perform(get("/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/product-detail"));

        assertThat(viewRepository.findByUserIdAndProductId(userId, productId))
                .isPresent();

        mockMvc.perform(post("/wishlist/" + productId).with(csrf()))
                .andExpect(redirectedUrl("/products/" + productId));

        assertThat(wishlistRepository.findByUserIdAndProductId(userId, productId))
                .isPresent();

        mockMvc.perform(get("/wishlist"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/saved-products"));

        mockMvc.perform(get("/recently-viewed"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/saved-products"));

        mockMvc.perform(post("/wishlist/" + productId + "/delete")
                .with(csrf()))
                .andExpect(redirectedUrl("/wishlist"));

        assertThat(wishlistRepository.findByUserIdAndProductId(userId, productId))
                .isEmpty();
    }
}
