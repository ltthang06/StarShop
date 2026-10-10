package vn.iotstar.starshop.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.CustomerCartLine;
import vn.iotstar.starshop.dto.CustomerCartSummary;
import vn.iotstar.starshop.entity.Cart;
import vn.iotstar.starshop.entity.CartItem;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.ProductImage;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.repository.CustomerCartItemRepository;
import vn.iotstar.starshop.repository.CustomerCartRepository;
import vn.iotstar.starshop.repository.CustomerCatalogRepository;
import vn.iotstar.starshop.repository.ProductImageRepository;
import vn.iotstar.starshop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomerCartService {

    private final UserRepository userRepository;
    private final CustomerCartRepository cartRepository;
    private final CustomerCartItemRepository cartItemRepository;
    private final CustomerCatalogRepository catalogRepository;
    private final ProductImageRepository imageRepository;

    @Transactional(readOnly = true)
    public CustomerCartSummary summary(String email) {
        User user = currentUser(email);
        List<CartItem> items = cartRepository.findByUserId(user.getId())
                .map(cart -> cartItemRepository.findByCartIdOrderByIdAsc(
                        cart.getId()))
                .orElseGet(List::of);

        List<CustomerCartLine> lines = items.stream()
                .map(this::toLine)
                .toList();
        BigDecimal subtotal = lines.stream()
                .map(CustomerCartLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean ready = !lines.isEmpty() && lines.stream()
                .allMatch(CustomerCartLine::isAvailable);
        long shopCount = items.stream()
                .map(item -> item.getProduct().getShop().getId())
                .distinct()
                .count();

        return new CustomerCartSummary(lines, subtotal, ready, shopCount);
    }

    @Transactional
    public void add(String email, Long productId, int quantity) {
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("Số lượng phải từ 1 đến 99");
        }
        User user = currentUser(email);
        Product product = publicProduct(productId);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        CartItem item = cartItemRepository.findByCartIdAndProductId(
                cart.getId(), productId).orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    return newItem;
                });

        int requested = item.getQuantity() + quantity;
        validateQuantity(requested, product.getQuantity());
        item.setQuantity(requested);
        cartItemRepository.save(item);
    }

    @Transactional
    public void update(String email, Long itemId, int quantity) {
        User user = currentUser(email);
        CartItem item = ownedItem(user.getId(), itemId);
        Product product = publicProduct(item.getProduct().getId());
        validateQuantity(quantity, product.getQuantity());
        item.setQuantity(quantity);
    }

    @Transactional
    public void remove(String email, Long itemId) {
        User user = currentUser(email);
        cartItemRepository.delete(ownedItem(user.getId(), itemId));
    }

    private CustomerCartLine toLine(CartItem item) {
        Product product = item.getProduct();
        BigDecimal price = product.getDiscountPrice() == null
                ? product.getPrice() : product.getDiscountPrice();
        boolean available = catalogRepository.findPublicById(
                product.getId(), ProductStatus.ACTIVE, ShopStatus.ACTIVE
        ).isPresent() && item.getQuantity() <= product.getQuantity();
        String imageUrl = imageRepository
                .findFirstByProductIdOrderByPrimaryImageDescIdAsc(
                        product.getId())
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return new CustomerCartLine(
                item.getId(),
                product.getId(),
                product.getName(),
                imageUrl,
                price,
                item.getQuantity(),
                price.multiply(BigDecimal.valueOf(item.getQuantity())),
                available
        );
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tài khoản"));
    }

    private Product publicProduct(Long id) {
        return catalogRepository.findPublicById(
                id, ProductStatus.ACTIVE, ShopStatus.ACTIVE
        ).orElseThrow(() -> new IllegalArgumentException(
                "Sản phẩm không còn bán"));
    }

    private CartItem ownedItem(Long userId, Long itemId) {
        return cartItemRepository.findByIdAndCart_User_Id(itemId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy sản phẩm trong giỏ hàng"));
    }

    private void validateQuantity(int requested, int stock) {
        if (requested < 1 || requested > 99 || requested > stock) {
            throw new IllegalArgumentException(
                    "Số lượng phải từ 1 đến " + Math.min(stock, 99));
        }
    }
}
