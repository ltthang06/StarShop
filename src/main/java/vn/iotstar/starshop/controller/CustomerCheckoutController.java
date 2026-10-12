package vn.iotstar.starshop.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.CustomerCartSummary;
import vn.iotstar.starshop.repository.CustomerShippingProviderRepository;
import vn.iotstar.starshop.service.CustomerAccountService;
import vn.iotstar.starshop.service.CustomerCartService;
import vn.iotstar.starshop.service.CustomerCheckoutService;
import vn.iotstar.starshop.service.CustomerPromotionService;
import vn.iotstar.starshop.entity.ShippingProvider;

@Controller
@RequiredArgsConstructor
public class CustomerCheckoutController {

    private final CustomerCartService cartService;
    private final CustomerAccountService accountService;
    private final CustomerCheckoutService checkoutService;
    private final CustomerShippingProviderRepository providerRepository;
    private final CustomerPromotionService promotionService;

    @GetMapping("/checkout")
    public String checkout(
            Authentication authentication,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) String code,
            Model model) {
        CustomerCartSummary cart = cartService.summary(
                authentication.getName());
        if (!cart.isReadyToCheckout()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("addresses", accountService.addresses(
                authentication.getName()));
        var providers = providerRepository
                .findByActiveTrueOrderByBaseFeeAsc();
        model.addAttribute("providers", providers);
        ShippingProvider selected = providers.stream()
                .filter(provider -> provider.getId().equals(providerId))
                .findFirst()
                .orElse(providers.isEmpty() ? null : providers.get(0));
        if (selected != null) {
            model.addAttribute("selectedProvider", selected);
            model.addAttribute("vouchers", promotionService.available(
                    authentication.getName(), selected.getId()));
            try {
                model.addAttribute("quote", promotionService.quote(
                        authentication.getName(), selected.getId(), code));
            } catch (IllegalArgumentException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
                model.addAttribute("quote", promotionService.quote(
                        authentication.getName(), selected.getId(), null));
            }
        }
        return "customer/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(
            Authentication authentication,
            @RequestParam Long addressId,
            @RequestParam Long providerId,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String code,
            RedirectAttributes redirectAttributes) {

        try {
            List<Long> orderIds = checkoutService.placeOrder(
                    authentication.getName(), addressId, providerId,
                    note, code);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã đặt " + orderIds.size() + " đơn hàng"
            );
            return "redirect:/orders";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
            return "redirect:/checkout";
        }
    }
}
