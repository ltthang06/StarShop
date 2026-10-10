package vn.iotstar.starshop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.service.CustomerCatalogService;
import vn.iotstar.starshop.service.CustomerActivityService;
import vn.iotstar.starshop.service.CustomerReviewService;

@Controller
@RequiredArgsConstructor
public class CustomerProductController {

    private final CustomerCatalogService catalogService;
    private final CustomerActivityService activityService;
    private final CustomerReviewService reviewService;

    @GetMapping({"/products", "/products/search"})
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        model.addAttribute("products",
                catalogService.search(keyword, categoryId, sort, page));
        model.addAttribute("categories", catalogService.categories());
        model.addAttribute("keyword", keyword.trim());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sort", sort);
        return "guest/products";
    }

    @GetMapping("/products/top")
    public String top20(
            @RequestParam(defaultValue = "newest") String sort,
            Model model) {

        model.addAttribute("products", catalogService.top20(sort));
        model.addAttribute("sort", sort);
        return "guest/top-products";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Authentication authentication,
            Model model) {
        model.addAttribute("product", catalogService.detail(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy sản phẩm")));
        model.addAttribute("reviews", reviewService.reviews(id));
        if (authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            activityService.recordView(authentication.getName(), id);
        }
        return "guest/product-detail";
    }
}
