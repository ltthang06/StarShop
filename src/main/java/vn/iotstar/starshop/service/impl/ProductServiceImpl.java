package vn.iotstar.starshop.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.ProductRequest;
import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.repository.CategoryRepository;
import vn.iotstar.starshop.repository.ProductRepository;
import vn.iotstar.starshop.repository.ShopRepository;
import vn.iotstar.starshop.service.ProductService;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchVendorProducts(
            Long shopId,
            Long ownerId,
            String keyword,
            Long categoryId,
            ProductStatus status,
            int page,
            int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 50));

        Specification<Product> specification =
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("shop").get("id"), shopId),
                        cb.equal(root.get("shop").get("owner").get("id"), ownerId)
                );

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            specification = specification.and(
                    (root, query, cb) -> cb.or(
                            cb.like(cb.lower(root.get("name")), pattern),
                            cb.like(cb.lower(root.get("description")), pattern)
                    )
            );
        }

        if (categoryId != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(root.get("category").get("id"), categoryId)
            );
        }

        if (status != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(root.get("status"), status)
            );
        }

        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return productRepository.findAll(specification, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductByOwner(
            Long productId,
            Long shopId,
            Long ownerId) {

        return productRepository
                .findByIdAndShopIdAndShopOwnerId(
                        productId,
                        shopId,
                        ownerId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy sản phẩm"
                        )
                );
    }

    @Override
    public Product createProduct(
            Long shopId,
            Long ownerId,
            ProductRequest request) {

        Shop shop = shopRepository
                .findByIdAndOwnerId(shopId, ownerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy cửa hàng"
                        )
                );

        Category category = getCategory(request.getCategoryId());

        validatePrice(request);

        Product product = new Product();

        applyRequest(product, request, category);

        product.setShop(shop);

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(
            Long productId,
            Long shopId,
            Long ownerId,
            ProductRequest request) {

        Product product = getProductByOwner(
                productId,
                shopId,
                ownerId
        );

        Category category = getCategory(request.getCategoryId());

        validatePrice(request);

        applyRequest(product, request, category);

        return productRepository.save(product);
    }

    @Override
    public void deactivateProduct(
            Long productId,
            Long shopId,
            Long ownerId) {

        Product product = getProductByOwner(
                productId,
                shopId,
                ownerId
        );

        product.setStatus(ProductStatus.INACTIVE);

        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc();
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository
                .findByIdAndActiveTrue(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Danh mục không hợp lệ"
                        )
                );
    }

    private void validatePrice(ProductRequest request) {

        if (request.getDiscountPrice() != null
                && request.getPrice() != null
                && request.getDiscountPrice()
                        .compareTo(request.getPrice()) > 0) {

            throw new IllegalArgumentException(
                    "Giá khuyến mãi không được lớn hơn giá bán"
            );
        }
    }

    private void applyRequest(
            Product product,
            ProductRequest request,
            Category category) {

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(category);

        ProductStatus requestedStatus =
                request.getStatus() == null
                        ? ProductStatus.ACTIVE
                        : request.getStatus();

        if (requestedStatus == ProductStatus.INACTIVE) {
            product.setStatus(ProductStatus.INACTIVE);
        } else if (request.getQuantity() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }
    }
}