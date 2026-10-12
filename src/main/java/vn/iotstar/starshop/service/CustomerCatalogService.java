package vn.iotstar.starshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.PublicProductCard;
import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.ProductImage;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.repository.CategoryRepository;
import vn.iotstar.starshop.repository.CustomerCatalogRepository;
import vn.iotstar.starshop.repository.ProductImageRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerCatalogService {

    private final CustomerCatalogRepository catalogRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository imageRepository;

    public List<PublicProductCard> featured() {
        return catalogRepository.findFeatured(
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                PageRequest.of(0, 8)
        ).stream().map(this::toCard).toList();
    }

    public List<PublicProductCard> top20(String sort) {
        if ("favorites".equals(sort)) {
            return catalogRepository.findMostWishlistedIds(
                    ProductStatus.ACTIVE,
                    ShopStatus.ACTIVE,
                    PageRequest.of(0, 20)
            ).stream()
                    .map(this::detail)
                    .flatMap(Optional::stream)
                    .toList();
        }

        return catalogRepository.search(
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                "",
                null,
                PageRequest.of(0, 20, sortOrder(sort))
        ).stream().map(this::toCard).toList();
    }

    public Page<PublicProductCard> search(
            String keyword, Long categoryId, String sort, int page) {

        String text = keyword == null ? "" : keyword.trim();

        return catalogRepository.search(
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                text,
                categoryId,
                PageRequest.of(Math.max(page, 0), 12, sortOrder(sort))
        ).map(this::toCard);
    }

    public Optional<PublicProductCard> detail(Long id) {
        return catalogRepository.findPublicById(
                id,
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE
        ).map(this::toCard);
    }

    public List<Category> categories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc();
    }

    private Sort sortOrder(String sort) {
        if ("bestseller".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "soldCount", "id");
        }
        if ("rating".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "rating", "id");
        }
        if ("price-asc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "price", "id");
        }
        if ("price-desc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "price", "id");
        }
        return Sort.by(Sort.Direction.DESC, "createdAt", "id");
    }

    private PublicProductCard toCard(Product product) {
        String imageUrl = imageRepository
                .findFirstByProductIdOrderByPrimaryImageDescIdAsc(
                        product.getId())
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return new PublicProductCard(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getDiscountPrice(),
                product.getQuantity(),
                product.getSoldCount(),
                product.getRating(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                imageUrl
        );
    }
}
