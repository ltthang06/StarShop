package vn.iotstar.starshop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import vn.iotstar.starshop.dto.CustomerCartSummary;
import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.CategoryRepository;
import vn.iotstar.starshop.repository.CustomerCatalogRepository;
import vn.iotstar.starshop.repository.ShopRepository;
import vn.iotstar.starshop.repository.UserRepository;
import vn.iotstar.starshop.service.CustomerCartService;

@SpringBootTest
class CustomerCartFlowTests {

    @Autowired
    private CustomerCartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerCatalogRepository catalogRepository;

    @Test
    void cartKeepsQuantityAndChecksOwnershipAndStock() {
        User customer = createUser("cart@example.com", "0900000011");
        User other = createUser("other-cart@example.com", "0900000012");
        User vendor = createUser("vendor-cart@example.com", "0900000013");

        Shop shop = new Shop();
        shop.setName("Tiệm hoa giỏ hàng");
        shop.setOwner(vendor);
        shop.setStatus(ShopStatus.ACTIVE);
        shop = shopRepository.save(shop);

        Category category = new Category();
        category.setName("Hoa giỏ hàng");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Hoa cẩm chướng");
        product.setDescription("Bó hoa cẩm chướng");
        product.setPrice(BigDecimal.valueOf(100000));
        product.setDiscountPrice(BigDecimal.valueOf(80000));
        product.setQuantity(4);
        product.setShop(shop);
        product.setCategory(category);
        product = catalogRepository.save(product);
        Long productId = product.getId();

        cartService.add(customer.getEmail(), productId, 2);
        cartService.add(customer.getEmail(), productId, 1);

        CustomerCartSummary cart = cartService.summary(customer.getEmail());
        assertThat(cart.getLines()).hasSize(1);
        assertThat(cart.getLines().get(0).getQuantity()).isEqualTo(3);
        assertThat(cart.getSubtotal()).isEqualByComparingTo("240000");

        Long itemId = cart.getLines().get(0).getId();
        assertThatThrownBy(() -> cartService.remove(other.getEmail(), itemId))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cartService.add(
                customer.getEmail(), productId, 2))
                .isInstanceOf(IllegalArgumentException.class);

        cartService.update(customer.getEmail(), itemId, 1);
        assertThat(cartService.summary(customer.getEmail()).getSubtotal())
                .isEqualByComparingTo("80000");

        product.setQuantity(0);
        catalogRepository.save(product);
        assertThat(cartService.summary(customer.getEmail()).isReadyToCheckout())
                .isFalse();

        cartService.remove(customer.getEmail(), itemId);
        assertThat(cartService.summary(customer.getEmail()).getLines())
                .isEmpty();
    }

    private User createUser(String email, String phone) {
        User user = new User();
        user.setFullName(email);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword("unused");
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }
}
