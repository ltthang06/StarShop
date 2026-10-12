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
import vn.iotstar.starshop.service.CustomerOrderService;

@Controller
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService orderService;

    @GetMapping("/orders")
    public String history(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        model.addAttribute("orders", orderService.history(
                authentication.getName(), page));
        return "customer/orders";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(
            Authentication authentication,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            orderService.cancel(authentication.getName(), id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Đã hủy đơn hàng");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/orders/{id}/return")
    public String requestReturn(
            Authentication authentication,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            orderService.requestReturn(authentication.getName(), id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Đã gửi yêu cầu trả hàng");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }
        return "redirect:/orders";
    }
}
