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
        return toCards(catalogRepository.findFeatured(
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                PageRequest.of(0, 8)
        ));
    }

    public List<PublicProductCard> top20(String sort) {
        if ("favorites".equals(sort)) {
            List<Long> rankedIds = catalogRepository.findMostWishlistedIds(
                    ProductStatus.ACTIVE,
                    ShopStatus.ACTIVE,
                    PageRequest.of(0, 20)
            );
            return publicCards(rankedIds);
        }

        return toCards(catalogRepository.search(
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                "",
                null,
                PageRequest.of(0, 20, sortOrder(sort))
        ).getContent());
    }

    public Page<PublicProductCard> top20(String sort, int page) {

        List<PublicProductCard> ranked = top20(sort);
        int size = 8;
        int lastPage = Math.max(0, (ranked.size() - 1) / size);
        int current = Math.min(Math.max(0, page), lastPage);
        int start = current * size;
        return new org.springframework.data.domain.PageImpl<>(
                ranked.subList(start, Math.min(start + size, ranked.size())),
                PageRequest.of(current, size), ranked.size());
    }

    public Page<PublicProductCard> search(
            String keyword, Long categoryId, String sort, int page) {

        String text = keyword == null ? "" : keyword.trim();

        Page<Product> result = catalogRepository.search(
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                text,
                categoryId,
                PageRequest.of(Math.max(page, 0), 12, sortOrder(sort))
        );
        var cards = toCards(result.getContent());
        return new org.springframework.data.domain.PageImpl<>(cards, result.getPageable(), result.getTotalElements());
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

    public List<PublicProductCard> publicCards(List<Long> orderedIds) {
        if (orderedIds.isEmpty()) return List.of();
        var byId = catalogRepository.findPublicByIds(orderedIds, ProductStatus.ACTIVE, ShopStatus.ACTIVE)
                .stream().collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));
        return toCards(orderedIds.stream().map(byId::get).filter(java.util.Objects::nonNull).toList());
    }

    private Sort sortOrder(String sort) {
        if ("bestseller".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "soldCount", "id");
        }
        if ("rating".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "rating", "id");
        }
        if ("price-asc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "effectivePrice", "id");
        }
        if ("price-desc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "effectivePrice", "id");
        }
        return Sort.by(Sort.Direction.DESC, "createdAt", "id");
    }

    private List<PublicProductCard> toCards(List<Product> products) {
        if (products.isEmpty()) return List.of();
        var urls = new java.util.HashMap<Long, String>();

        imageRepository.findByProductIdInOrderByPrimaryImageDescIdAsc(
                products.stream().map(Product::getId).toList())
                .forEach(image -> urls.putIfAbsent(image.getProduct().getId(), image.getImageUrl()));
        return products.stream().map(p -> toCard(p, urls.get(p.getId()))).toList();
    }

    private PublicProductCard toCard(Product product) {
        String imageUrl = imageRepository
                .findFirstByProductIdOrderByPrimaryImageDescIdAsc(
                        product.getId())
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return toCard(product, imageUrl);
    }

    private PublicProductCard toCard(Product product, String imageUrl) {
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
