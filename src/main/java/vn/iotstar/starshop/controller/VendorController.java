package vn.iotstar.starshop.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.ShopRequest;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.repository.UserRepository;
import vn.iotstar.starshop.service.ShopService;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final ShopService shopService;
    private final UserRepository userRepository;

    @GetMapping
    @ResponseBody
    public String vendorHome() {
        return "Vendor module is running";
    }

    @GetMapping("/shop/register")
    public String showRegisterShopForm(Model model) {
        if (!model.containsAttribute("shopRequest")) {
            model.addAttribute("shopRequest", new ShopRequest());
        }

        return "vendor/shop-register";
    }

    @PostMapping("/shop/register")
    public String registerShop(
            @Valid @ModelAttribute("shopRequest") ShopRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "errorMessage",
                    "Thông tin đăng ký chưa hợp lệ. Vui lòng kiểm tra lại."
            );
            return "vendor/shop-register";
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            model.addAttribute(
                    "errorMessage",
                    "Bạn cần đăng nhập trước khi đăng ký cửa hàng."
            );
            return "vendor/shop-register";
        }

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElse(null);

        if (owner == null) {
            model.addAttribute(
                    "errorMessage",
                    "Không tìm thấy tài khoản người dùng trong hệ thống."
            );
            return "vendor/shop-register";
        }

        Shop shop = shopService.registerShop(owner, request);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đăng ký cửa hàng thành công. Cửa hàng đang chờ duyệt."
        );

        redirectAttributes.addFlashAttribute(
                "registeredShopId",
                shop.getId()
        );

        return "redirect:/vendor/shop/register";
    }
}