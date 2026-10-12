package vn.iotstar.starshop.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.entity.Address;
import vn.iotstar.starshop.service.CustomerAccountService;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerAccountService accountService;

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        String email = authentication.getName();
        model.addAttribute("user", accountService.profile(email));
        model.addAttribute("addresses", accountService.addresses(email));
        return "customer/profile";
    }

    @PostMapping
    public String updateProfile(
            Authentication authentication,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            RedirectAttributes redirectAttributes) {

        try {
            accountService.updateProfile(
                    authentication.getName(),
                    fullName,
                    phone
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã cập nhật hồ sơ"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }
        return "redirect:/profile";
    }

    @GetMapping("/addresses/new")
    public String newAddress(Model model) {
        model.addAttribute("address", new Address());
        model.addAttribute("formAction", "/profile/addresses");
        return "customer/address-form";
    }

    @GetMapping("/addresses/{id}/edit")
    public String editAddress(
            Authentication authentication,
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "address",
                accountService.address(authentication.getName(), id)
        );
        model.addAttribute("formAction", "/profile/addresses/" + id);
        return "customer/address-form";
    }

    @PostMapping("/addresses")
    public String createAddress(
            Authentication authentication,
            @RequestParam String recipientName,
            @RequestParam String phone,
            @RequestParam String addressLine,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String province,
            @RequestParam(defaultValue = "false") boolean makeDefault,
            RedirectAttributes redirectAttributes) {

        return saveAddress(
                authentication.getName(),
                null,
                recipientName,
                phone,
                addressLine,
                ward,
                district,
                province,
                makeDefault,
                redirectAttributes
        );
    }

    @PostMapping("/addresses/{id}")
    public String updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam String recipientName,
            @RequestParam String phone,
            @RequestParam String addressLine,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String province,
            @RequestParam(defaultValue = "false") boolean makeDefault,
            RedirectAttributes redirectAttributes) {

        return saveAddress(
                authentication.getName(),
                id,
                recipientName,
                phone,
                addressLine,
                ward,
                district,
                province,
                makeDefault,
                redirectAttributes
        );
    }

    @PostMapping("/addresses/{id}/default")
    public String setDefault(
            Authentication authentication,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            accountService.setDefault(authentication.getName(), id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã đổi địa chỉ mặc định"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }
        return "redirect:/profile";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(
            Authentication authentication,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            accountService.deleteAddress(authentication.getName(), id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã xóa địa chỉ"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }
        return "redirect:/profile";
    }

    private String saveAddress(
            String email,
            Long id,
            String recipientName,
            String phone,
            String addressLine,
            String ward,
            String district,
            String province,
            boolean makeDefault,
            RedirectAttributes redirectAttributes) {

        try {
            accountService.saveAddress(
                    email,
                    id,
                    recipientName,
                    phone,
                    addressLine,
                    ward,
                    district,
                    province,
                    makeDefault
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã lưu địa chỉ"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }
        return "redirect:/profile";
    }
}
