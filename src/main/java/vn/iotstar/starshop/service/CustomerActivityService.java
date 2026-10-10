package vn.iotstar.starshop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.PublicProductCard;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.ProductView;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.entity.Wishlist;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.repository.CustomerCatalogRepository;
import vn.iotstar.starshop.repository.CustomerProductViewRepository;
import vn.iotstar.starshop.repository.CustomerWishlistRepository;
import vn.iotstar.starshop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomerActivityService {

    private final UserRepository userRepository;
    private final CustomerCatalogRepository catalogRepository;
    private final CustomerCatalogService catalogService;
    private final CustomerWishlistRepository wishlistRepository;
    private final CustomerProductViewRepository viewRepository;

    @Transactional
    public void recordView(String email, Long productId) {
        User user = currentUser(email);
        Product product = publicProduct(productId);

        ProductView view = viewRepository.findByUserIdAndProductId(
                user.getId(), productId
        ).orElseGet(() -> {
            ProductView newView = new ProductView();
            newView.setUser(user);
            newView.setProduct(product);
            return newView;
        });

        view.setViewedAt(LocalDateTime.now());
        viewRepository.save(view);
    }

    @Transactional
    public void addToWishlist(String email, Long productId) {
        User user = currentUser(email);
        Product product = publicProduct(productId);

        if (wishlistRepository.findByUserIdAndProductId(
                user.getId(), productId
        ).isPresent()) {
            return;
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlistRepository.save(wishlist);
    }

    @Transactional
    public void removeFromWishlist(String email, Long productId) {
        User user = currentUser(email);
        wishlistRepository.findByUserIdAndProductId(
                user.getId(), productId
        ).ifPresent(wishlistRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<PublicProductCard> wishlist(String email) {
        User user = currentUser(email);
        return wishlistRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(wishlist -> catalogService.detail(
                        wishlist.getProduct().getId()))
                .flatMap(Optional::stream)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicProductCard> recentlyViewed(String email) {
        User user = currentUser(email);
        return viewRepository
                .findByUserIdOrderByViewedAtDesc(
                        user.getId(),
                        PageRequest.of(0, 20))
                .stream()
                .map(view -> catalogService.detail(
                        view.getProduct().getId()))
                .flatMap(Optional::stream)
                .toList();
    }

    private User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tài khoản"));
    }

    private Product publicProduct(Long id) {
        return catalogRepository.findPublicById(
                id,
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE
        ).orElseThrow(() -> new IllegalArgumentException(
                "Sản phẩm không còn bán"));
    }
}
