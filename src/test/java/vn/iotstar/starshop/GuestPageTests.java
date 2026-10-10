package vn.iotstar.starshop;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.ServletContext;
import vn.iotstar.starshop.entity.Role;
import vn.iotstar.starshop.entity.Address;
import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.Order;
import vn.iotstar.starshop.entity.ShippingProvider;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.RoleName;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.RoleRepository;
import vn.iotstar.starshop.repository.CategoryRepository;
import vn.iotstar.starshop.repository.CustomerAddressRepository;
import vn.iotstar.starshop.repository.CustomerCatalogRepository;
import vn.iotstar.starshop.repository.CustomerShippingProviderRepository;
import vn.iotstar.starshop.repository.ShopRepository;
import vn.iotstar.starshop.repository.OrderDetailRepository;
import vn.iotstar.starshop.repository.OrderRepository;
import vn.iotstar.starshop.repository.UserRepository;
import vn.iotstar.starshop.service.CustomerCartService;
import vn.iotstar.starshop.service.CustomerCheckoutService;
import vn.iotstar.starshop.service.CustomerReviewService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GuestPageTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ServletContext servletContext;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerCatalogRepository productRepository;

    @Autowired
    private CustomerAddressRepository addressRepository;

    @Autowired
    private CustomerShippingProviderRepository providerRepository;

    @Autowired
    private CustomerCartService cartService;

    @Autowired
    private CustomerCheckoutService checkoutService;

    @Autowired
    private CustomerReviewService reviewService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository detailRepository;

    @Test
    void publicPagesRender() throws Exception {
        assertThat(servletContext.getResource(
                "/WEB-INF/views/guest/home.jsp")).isNotNull();

        String[] paths = {
                "/",
                "/products",
                "/products/top?sort=favorites",
                "/login",
                "/register",
                "/verify-otp?email=customer%40example.com",
                "/forgot-password",
                "/reset-password"
        };

        for (String path : paths) {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    path,
                    String.class
            );
            assertThat(response.getStatusCode().is2xxSuccessful())
                    .as(path + ": " + response.getStatusCode()
                            + " " + response.getBody())
                    .isTrue();
        }
    }

    @Test
    void customerPagesRenderAfterLogin() throws Exception {
        Role role = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.USER);
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setFullName("Khách kiểm tra giao diện");
        user.setEmail("page-test@example.com");
        user.setPhone("0900000099");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(role));
        userRepository.save(user);

        Shop shop = new Shop();
        shop.setName("Shop kiểm tra trang");
        shop.setOwner(user);
        shop.setStatus(ShopStatus.ACTIVE);
        shop = shopRepository.save(shop);

        Category category = new Category();
        category.setName("Danh mục kiểm tra trang");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Hoa trang kiểm tra");
        product.setDescription("Hoa để kiểm tra trang đặt hàng");
        product.setPrice(BigDecimal.valueOf(100000));
        product.setQuantity(5);
        product.setShop(shop);
        product.setCategory(category);
        product = productRepository.save(product);
        cartService.add(user.getEmail(), product.getId(), 1);

        Address address = new Address();
        address.setUser(user);
        address.setRecipientName("Người nhận");
        address.setPhone("0900000099");
        address.setAddressLine("Số 1 Đường Hoa");
        address.setDefaultAddress(true);
        address = addressRepository.save(address);

        ShippingProvider provider = new ShippingProvider();
        provider.setName("Giao hàng kiểm tra trang");
        provider.setBaseFee(BigDecimal.valueOf(15000));
        provider = providerRepository.save(provider);

        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null,
                        CookiePolicy.ACCEPT_ALL))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        URI loginUri = URI.create("http://localhost:" + port + "/login");
        String loginPage = client.send(
                HttpRequest.newBuilder(loginUri).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        ).body();

        Matcher token = Pattern.compile(
                "name=\"_csrf\" value=\"([^\"]+)\"")
                .matcher(loginPage);
        assertThat(token.find()).isTrue();

        String form = "email=page-test%40example.com"
                + "&password=password123"
                + "&_csrf=" + token.group(1);
        HttpResponse<String> loginResponse = client.send(
                HttpRequest.newBuilder(loginUri)
                        .header("Content-Type",
                                "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(form))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(loginResponse.statusCode()).isEqualTo(200);

        for (String path : new String[] {
                "/cart", "/checkout", "/wishlist", "/recently-viewed",
                "/orders"
        }) {
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder(URI.create(
                            "http://localhost:" + port + path))
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            assertThat(response.statusCode())
                    .as(path + ": " + response.body())
                    .isEqualTo(200);
        }

        Long orderId = checkoutService.placeOrder(
                user.getEmail(), address.getId(), provider.getId(), null)
                .get(0);
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
        Long detailId = detailRepository.findByOrderIdOrderByIdAsc(
                orderId).get(0).getId();

        String reviewPath = "/orders/" + orderId
                + "/items/" + detailId + "/review";
        HttpResponse<String> reviewPage = client.send(
                HttpRequest.newBuilder(URI.create(
                        "http://localhost:" + port + reviewPath))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(reviewPage.statusCode())
                .as(reviewPage.body()).isEqualTo(200);

        reviewService.submit(user.getEmail(), orderId, detailId, 5,
                "Sản phẩm đẹp, đóng gói kỹ và giao hàng đúng hẹn. "
                        + "Tôi rất hài lòng với lần mua này.",
                null, null);
        HttpResponse<String> productPage = client.send(
                HttpRequest.newBuilder(URI.create(
                        "http://localhost:" + port
                                + "/products/" + product.getId()))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertThat(productPage.statusCode())
                .as(productPage.body()).isEqualTo(200);
    }
}
