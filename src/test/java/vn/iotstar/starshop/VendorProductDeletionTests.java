package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.PaymentMethod;
import vn.iotstar.starshop.service.ProductService;

@SpringBootTest
@Transactional
class VendorProductDeletionTests {
    @Autowired EntityManager em;
    @Autowired ProductService service;

    @Test void deletesUnusedProductAndDependentCustomerEntries() {
        Product product = product();
        Long id = product.getId(), shopId = product.getShop().getId();
        User owner = product.getShop().getOwner();
        Cart cart = new Cart(); cart.setUser(owner); em.persist(cart);
        CartItem item = new CartItem(); item.setCart(cart); item.setProduct(product); item.setQuantity(1); em.persist(item);
        Wishlist wishlist = new Wishlist(); wishlist.setUser(owner); wishlist.setProduct(product); em.persist(wishlist);
        ProductView view = new ProductView(); view.setUser(owner); view.setProduct(product); em.persist(view);
        ProductImage image = new ProductImage(); image.setProduct(product); image.setImageUrl("https://example.test/flower.webp"); em.persist(image);
        em.flush();
        service.deleteProduct(id, shopId, owner.getId());
        em.clear();
        assertNull(em.find(Product.class, id));
        for (String entity : java.util.List.of("CartItem", "Wishlist", "ProductView", "ProductImage")) {
            assertEquals(0L, em.createQuery("select count(e) from " + entity + " e where e.product.id = :id", Long.class)
                    .setParameter("id", id).getSingleResult());
        }
        assertNotNull(em.find(Cart.class, cart.getId()));
    }

    @Test void preservesProductWithOrderEvenWhenSoldCountIsZero() {
        Product product = product();
        Order order = new Order(); order.setShop(product.getShop()); order.setUser(product.getShop().getOwner());
        order.setReceiverName("Buyer"); order.setReceiverPhone("0901234567"); order.setShippingAddress("Test address");
        order.setSubtotal(product.getPrice()); order.setTotalAmount(product.getPrice()); order.setPaymentMethod(PaymentMethod.COD); em.persist(order);
        OrderDetail detail = new OrderDetail(); detail.setOrder(order); detail.setProduct(product); detail.setQuantity(1);
        detail.setUnitPrice(product.getPrice()); detail.setSubtotal(product.getPrice()); em.persist(detail); em.flush();
        assertThrows(IllegalArgumentException.class, () -> service.deleteProduct(product.getId(), product.getShop().getId(), product.getShop().getOwner().getId()));
        assertNotNull(em.find(Product.class, product.getId()));
        assertNotNull(em.find(OrderDetail.class, detail.getId()));
    }

    @Test void anotherVendorCannotDeleteProduct() {
        Product product = product();
        User other = user();
        assertThrows(IllegalArgumentException.class, () -> service.deleteProduct(product.getId(), product.getShop().getId(), other.getId()));
        assertNotNull(em.find(Product.class, product.getId()));
    }

    @Test void preservesSeededSalesHistory() {
        Product product = product(); product.setSoldCount(1); em.flush();
        assertThrows(IllegalArgumentException.class, () -> service.deleteProduct(product.getId(), product.getShop().getId(), product.getShop().getOwner().getId()));
        assertNotNull(em.find(Product.class, product.getId()));
    }

    private User user() {
        User user = new User(); user.setFullName("Vendor"); user.setEmail(UUID.randomUUID() + "@test.local");
        user.setPassword("test-password"); em.persist(user); return user;
    }

    private Product product() {
        Shop shop = new Shop(); shop.setName("Deletion test"); shop.setOwner(user()); em.persist(shop);
        Category category = new Category(); category.setName("Flowers " + UUID.randomUUID()); em.persist(category);
        Product product = new Product(); product.setName("Unused flower"); product.setDescription("Deletion test flower");
        product.setShop(shop); product.setCategory(category); product.setPrice(new BigDecimal("100000"));
        product.setQuantity(5); em.persist(product); em.flush(); return product;
    }
}
