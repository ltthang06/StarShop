package vn.iotstar.starshop.service;

import java.util.List;

import org.springframework.data.domain.Page;

import vn.iotstar.starshop.dto.ProductRequest;
import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.enums.ProductStatus;

public interface ProductService {

    Page<Product> searchVendorProducts(
            Long shopId,
            Long ownerId,
            String keyword,
            Long categoryId,
            ProductStatus status,
            int page,
            int size
    );

    Product getProductByOwner(
            Long productId,
            Long shopId,
            Long ownerId
    );

    Product createProduct(
            Long shopId,
            Long ownerId,
            ProductRequest request
    );

    Product updateProduct(
            Long productId,
            Long shopId,
            Long ownerId,
            ProductRequest request
    );

    void deactivateProduct(
            Long productId,
            Long shopId,
            Long ownerId
    );

    List<Category> getActiveCategories();
}