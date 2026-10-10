package vn.iotstar.starshop.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.service.CustomerActivityService;

@Controller
@RequiredArgsConstructor
public class CustomerActivityController {

    private final CustomerActivityService activityService;

    @GetMapping("/wishlist")
    public String wishlist(Authentication authentication, Model model) {
        model.addAttribute(
                "products",
                activityService.wishlist(authentication.getName())
        );
        model.addAttribute("pageTitle", "Sản phẩm yêu thích");
        model.addAttribute("wishlistPage", true);
        return "customer/saved-products";
    }

    @GetMapping("/recently-viewed")
    public String recentlyViewed(
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "products",
                activityService.recentlyViewed(authentication.getName())
        );
        model.addAttribute("pageTitle", "Sản phẩm đã xem");
        model.addAttribute("wishlistPage", false);
        return "customer/saved-products";
    }

    @PostMapping("/wishlist/{productId}")
    public String addWishlist(
            Authentication authentication,
            @PathVariable Long productId,
            RedirectAttributes redirectAttributes) {

        try {
            activityService.addToWishlist(
                    authentication.getName(), productId
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã thêm vào yêu thích"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/wishlist/{productId}/delete")
    public String removeWishlist(
            Authentication authentication,
            @PathVariable Long productId) {

        activityService.removeFromWishlist(
                authentication.getName(), productId
        );
        return "redirect:/wishlist";
    }
}
