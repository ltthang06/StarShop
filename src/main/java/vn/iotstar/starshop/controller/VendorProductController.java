package vn.iotstar.starshop.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.ProductRequest;
import vn.iotstar.starshop.entity.Product;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.ProductStatus;
import vn.iotstar.starshop.repository.UserRepository;
import vn.iotstar.starshop.service.ProductService;
import vn.iotstar.starshop.service.ShopService;

@Controller
@RequestMapping("/vendor/shops/{shopId}/products")
@RequiredArgsConstructor
public class VendorProductController {

    private final ProductService productService;
    private final ShopService shopService;
    private final UserRepository userRepository;

    @GetMapping
    public String showProducts(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        User owner = getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        Shop shop = shopService.getShopByOwner(
                shopId,
                owner.getId()
        );

        ProductStatus statusValue = parseStatus(status);

        Page<Product> products =
                productService.searchVendorProducts(
                        shopId,
                        owner.getId(),
                        keyword,
                        categoryId,
                        statusValue,
                        page,
                        size
                );

        model.addAttribute("shop", shop);
        model.addAttribute("products", products);
        model.addAttribute("categories",
                productService.getActiveCategories());
        model.addAttribute("statuses",
                ProductStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("selectedStatus", statusValue);
        model.addAttribute("size", size);

        return "vendor/product-list";
    }

    @GetMapping("/create")
    public String showCreateForm(
            @PathVariable Long shopId,
            Authentication authentication,
            Model model) {

        User owner = getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        Shop shop = shopService.getShopByOwner(
                shopId,
                owner.getId()
        );

        ProductRequest request = new ProductRequest();
        request.setQuantity(1);
        request.setStatus(ProductStatus.ACTIVE);

        model.addAttribute("shop", shop);
        model.addAttribute("productRequest", request);
        model.addAttribute("categories",
                productService.getActiveCategories());
        model.addAttribute("statuses",
                ProductStatus.values());
        model.addAttribute(
                "formAction",
                "/vendor/shops/" + shopId + "/products/create"
        );
        model.addAttribute("editing", false);

        return "vendor/product-form";
    }

    @PostMapping("/create")
    public String createProduct(
            @PathVariable Long shopId,
            @Valid @ModelAttribute("productRequest")
            ProductRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        User owner = getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        Shop shop = shopService.getShopByOwner(
                shopId,
                owner.getId()
        );

        validateDiscount(request, bindingResult);

        if (bindingResult.hasErrors()) {
            prepareForm(
                    model,
                    shop,
                    request,
                    "/vendor/shops/" + shopId + "/products/create",
                    false
            );

            return "vendor/product-form";
        }

        try {
            productService.createProduct(
                    shopId,
                    owner.getId(),
                    request
            );
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());

            prepareForm(
                    model,
                    shop,
                    request,
                    "/vendor/shops/" + shopId + "/products/create",
                    false
            );

            return "vendor/product-form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Thêm sản phẩm thành công."
        );

        return "redirect:/vendor/shops/"
                + shopId
                + "/products";
    }

    @GetMapping("/{productId}/edit")
    public String showEditForm(
            @PathVariable Long shopId,
            @PathVariable Long productId,
            Authentication authentication,
            Model model) {

        User owner = getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        Shop shop = shopService.getShopByOwner(
                shopId,
                owner.getId()
        );

        Product product = productService.getProductByOwner(
                productId,
                shopId,
                owner.getId()
        );

        ProductRequest request = new ProductRequest();

        request.setName(product.getName());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice());
        request.setDiscountPrice(product.getDiscountPrice());
        request.setQuantity(product.getQuantity());
        request.setCategoryId(product.getCategory().getId());
        request.setStatus(product.getStatus());

        model.addAttribute("shop", shop);
        model.addAttribute("productRequest", request);
        model.addAttribute("categories",
                productService.getActiveCategories());
        model.addAttribute("statuses",
                ProductStatus.values());
        model.addAttribute(
                "formAction",
                "/vendor/shops/"
                        + shopId
                        + "/products/"
                        + productId
                        + "/edit"
        );
        model.addAttribute("editing", true);

        return "vendor/product-form";
    }

    @PostMapping("/{productId}/edit")
    public String updateProduct(
            @PathVariable Long shopId,
            @PathVariable Long productId,
            @Valid @ModelAttribute("productRequest")
            ProductRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        User owner = getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        Shop shop = shopService.getShopByOwner(
                shopId,
                owner.getId()
        );

        validateDiscount(request, bindingResult);

        if (bindingResult.hasErrors()) {
            prepareForm(
                    model,
                    shop,
                    request,
                    "/vendor/shops/"
                            + shopId
                            + "/products/"
                            + productId
                            + "/edit",
                    true
            );

            return "vendor/product-form";
        }

        try {
            productService.updateProduct(
                    productId,
                    shopId,
                    owner.getId(),
                    request
            );
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());

            prepareForm(
                    model,
                    shop,
                    request,
                    "/vendor/shops/"
                            + shopId
                            + "/products/"
                            + productId
                            + "/edit",
                    true
            );

            return "vendor/product-form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Cập nhật sản phẩm thành công."
        );

        return "redirect:/vendor/shops/"
                + shopId
                + "/products";
    }

    @PostMapping("/{productId}/delete")
    public String deactivateProduct(
            @PathVariable Long shopId,
            @PathVariable Long productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User owner = getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        productService.deactivateProduct(
                productId,
                shopId,
                owner.getId()
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Sản phẩm đã chuyển sang trạng thái INACTIVE."
        );

        return "redirect:/vendor/shops/"
                + shopId
                + "/products";
    }

    private void prepareForm(
            Model model,
            Shop shop,
            ProductRequest request,
            String formAction,
            boolean editing) {

        model.addAttribute("shop", shop);
        model.addAttribute("productRequest", request);
        model.addAttribute("categories",
                productService.getActiveCategories());
        model.addAttribute("statuses",
                ProductStatus.values());
        model.addAttribute("formAction", formAction);
        model.addAttribute("editing", editing);
    }

    private void validateDiscount(
            ProductRequest request,
            BindingResult bindingResult) {

        if (request.getPrice() != null
                && request.getDiscountPrice() != null
                && request.getDiscountPrice()
                        .compareTo(request.getPrice()) > 0) {

            bindingResult.rejectValue(
                    "discountPrice",
                    "invalid.discountPrice",
                    "Giá khuyến mãi không được lớn hơn giá bán"
            );
        }
    }

    private ProductStatus parseStatus(String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return ProductStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private User getCurrentUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return null;
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElse(null);
    }
}