package vn.iotstar.starshop.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.service.CustomerCartService;

@Controller
@RequiredArgsConstructor
public class CustomerCartController {

    private final CustomerCartService cartService;

    @GetMapping("/cart")
    public String cart(Authentication authentication, Model model) {
        model.addAttribute("cart", cartService.summary(authentication.getName()));
        return "customer/cart";
    }

    @PostMapping("/cart/items")
    public String add(
            Authentication authentication,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.add(authentication.getName(), productId, quantity);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Đã thêm vào giỏ hàng");
            return "redirect:/cart";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    @PostMapping("/cart/items/{itemId}")
    public String update(
            Authentication authentication,
            @PathVariable Long itemId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.update(authentication.getName(), itemId, quantity);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{itemId}/delete")
    public String remove(
            Authentication authentication,
            @PathVariable Long itemId,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.remove(authentication.getName(), itemId);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }
}
