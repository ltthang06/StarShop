package vn.iotstar.starshop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import vn.iotstar.starshop.entity.Address;
import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.entity.Order;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.Promotion;
import vn.iotstar.starshop.entity.ShippingProvider;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.PaymentMethod;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.enums.PromotionScope;
import vn.iotstar.starshop.enums.PromotionType;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.CategoryRepository;
import vn.iotstar.starshop.repository.CustomerAddressRepository;
import vn.iotstar.starshop.repository.CustomerCatalogRepository;
import vn.iotstar.starshop.repository.CustomerShippingProviderRepository;
import vn.iotstar.starshop.repository.OrderDetailRepository;
import vn.iotstar.starshop.repository.OrderRepository;
import vn.iotstar.starshop.repository.CustomerReviewRepository;
import vn.iotstar.starshop.repository.CustomerPromotionRepository;
import vn.iotstar.starshop.repository.ShopRepository;
import vn.iotstar.starshop.repository.UserRepository;
import vn.iotstar.starshop.service.CustomerCartService;
import vn.iotstar.starshop.service.CustomerCheckoutService;
import vn.iotstar.starshop.service.CustomerOrderService;
import vn.iotstar.starshop.service.CustomerReviewService;
import vn.iotstar.starshop.service.CustomerPromotionService;

@SpringBootTest
class CustomerCheckoutFlowTests {

    @Autowired
    private UserRepository userRepository;

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
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository detailRepository;

    @Autowired
    private CustomerCartService cartService;

    @Autowired
    private CustomerCheckoutService checkoutService;

    @Autowired
    private CustomerOrderService customerOrderService;

    @Autowired
    private CustomerReviewService reviewService;

    @Autowired
    private CustomerReviewRepository reviewRepository;

    @Autowired
    private CustomerPromotionRepository promotionRepository;

    @Autowired
    private CustomerPromotionService promotionService;

    @Test
    void checkoutSplitsOrdersByShopAndReservesStock() throws Exception {
        User customer = createUser("checkout@example.com", "0900000021");
        User firstVendor = createUser("vendor-one@example.com", "0900000022");
        User secondVendor = createUser("vendor-two@example.com", "0900000023");

        Category category = new Category();
        category.setName("Hoa thanh toán");
        category = categoryRepository.save(category);

        Product first = createProduct(firstVendor, category, "Hoa một", 2);
        Product second = createProduct(secondVendor, category, "Hoa hai", 3);

        Address address = new Address();
        address.setUser(customer);
        address.setRecipientName("Người nhận");
        address.setPhone("0900000021");
        address.setAddressLine("Số 1 Đường Hoa");
        address = addressRepository.save(address);

        ShippingProvider provider = new ShippingProvider();
        provider.setName("Đơn vị giao thử");
        provider.setBaseFee(BigDecimal.valueOf(15000));
        provider = providerRepository.save(provider);

        cartService.add(customer.getEmail(), first.getId(), 2);
        cartService.add(customer.getEmail(), second.getId(), 1);

        Promotion promotion = new Promotion();
        promotion.setCode("SAVE50");
        promotion.setName("Giảm 50 nghìn");
        promotion.setScope(PromotionScope.SYSTEM);
        promotion.setType(PromotionType.FIXED_AMOUNT);
        promotion.setDiscountValue(BigDecimal.valueOf(50000));
        promotion.setQuantity(2);
        promotion.setStartAt(LocalDateTime.now().minusDays(1));
        promotion.setEndAt(LocalDateTime.now().plusDays(1));
        promotion = promotionRepository.save(promotion);

        assertThat(promotionService.quote(
                customer.getEmail(), provider.getId(), "SAVE50")
                .getTotalAmount()).isEqualByComparingTo("280000");

        List<Long> orderIds = checkoutService.placeOrder(
                customer.getEmail(), address.getId(), provider.getId(),
                "Giao buổi sáng", "SAVE50");

        assertThat(orderIds).hasSize(2);
        assertThat(cartService.summary(customer.getEmail()).getLines())
                .isEmpty();
        assertThat(productRepository.findById(first.getId()).orElseThrow()
                .getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        assertThat(productRepository.findById(second.getId()).orElseThrow()
                .getQuantity()).isEqualTo(2);

        List<Order> orders = orderIds.stream()
                .map(id -> orderRepository.findById(id).orElseThrow())
                .toList();
        assertThat(orders).allSatisfy(order -> {
            assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.COD);
            assertThat(order.getShippingFee())
                    .isEqualByComparingTo("15000");
            assertThat(detailRepository.findByOrderIdOrderByIdAsc(
                    order.getId())).hasSize(1);
        });
        assertThat(orders.stream().map(Order::getTotalAmount))
                .containsExactlyInAnyOrder(
                        new BigDecimal("181666.67"),
                        new BigDecimal("98333.33"));
        assertThat(promotionRepository.findById(promotion.getId())
                .orElseThrow().getQuantity()).isEqualTo(1);

        customerOrderService.cancel(customer.getEmail(), orderIds.get(0));
        assertThat(orderRepository.findById(orderIds.get(0)).orElseThrow()
                .getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(productRepository.findById(first.getId()).orElseThrow()
                .getQuantity()).isEqualTo(2);

        Order delivered = orderRepository.findById(
                orderIds.get(1)).orElseThrow();
        delivered.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(delivered);

        Long detailId = detailRepository.findByOrderIdOrderByIdAsc(
                delivered.getId()).get(0).getId();
        String reviewText = "Hoa rất tươi, đóng gói cẩn thận và giao đúng hẹn. "
                + "Tôi sẽ tiếp tục mua lần sau.";
        reviewService.submit(customer.getEmail(), delivered.getId(),
                detailId, 5, reviewText, null, null);
        assertThat(reviewRepository.existsByOrderDetailId(detailId))
                .isTrue();
        assertThat(productRepository.findById(second.getId()).orElseThrow()
                .getRating()).isEqualTo(5);
        assertThatThrownBy(() -> reviewService.submit(
                customer.getEmail(), delivered.getId(), detailId,
                5, reviewText, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        customerOrderService.requestReturn(customer.getEmail(),
                delivered.getId());
        assertThat(orderRepository.findById(delivered.getId()).orElseThrow()
                .getStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);
    }

    private Product createProduct(
            User vendor, Category category, String name, int quantity) {

        Shop shop = new Shop();
        shop.setName(name + " shop");
        shop.setOwner(vendor);
        shop.setStatus(ShopStatus.ACTIVE);
        shop = shopRepository.save(shop);

        Product product = new Product();
        product.setName(name);
        product.setDescription(name);
        product.setPrice(BigDecimal.valueOf(100000));
        product.setQuantity(quantity);
        product.setShop(shop);
        product.setCategory(category);
        return productRepository.save(product);
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
