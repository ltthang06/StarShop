package vn.iotstar.starshop.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

        User owner = getCurrentUser(authentication);

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

    @GetMapping("/shops")
    public String showMyShops(
            Authentication authentication,
            Model model) {

        User owner = getCurrentUser(authentication);

        if (owner == null) {
            model.addAttribute(
                    "errorMessage",
                    "Không tìm thấy tài khoản người dùng."
            );
            return "vendor/shop-list";
        }

        List<Shop> shops = shopService.getShopsByOwner(owner.getId());

        model.addAttribute("shops", shops);

        return "vendor/shop-list";
    }

    @GetMapping("/shops/{id}")
    public String showShopDetail(
            @PathVariable("id") Long shopId,
            Authentication authentication,
            Model model) {

        User owner = getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor/shops";
        }

        Shop shop = shopService.getShopByOwner(
                shopId,
                owner.getId()
        );

        model.addAttribute("shop", shop);

        return "vendor/shop-detail";
    }

    @GetMapping("/shops/{id}/edit")
    public String showEditShopForm(
            @PathVariable("id") Long shopId,
            Authentication authentication,
            Model model) {

        User owner = getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor/shops";
        }

        Shop shop = shopService.getShopByOwner(
                shopId,
                owner.getId()
        );

        ShopRequest request = new ShopRequest();
        request.setName(shop.getName());
        request.setDescription(shop.getDescription());
        request.setPhone(shop.getPhone());
        request.setEmail(shop.getEmail());
        request.setAddress(shop.getAddress());
        request.setLogo(shop.getLogo());
        request.setBanner(shop.getBanner());

        model.addAttribute("shop", shop);
        model.addAttribute("shopRequest", request);

        return "vendor/shop-edit";
    }

    @PostMapping("/shops/{id}/edit")
    public String updateShop(
            @PathVariable("id") Long shopId,
            @Valid @ModelAttribute("shopRequest") ShopRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        User owner = getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor/shops";
        }

        if (bindingResult.hasErrors()) {
            Shop shop = shopService.getShopByOwner(
                    shopId,
                    owner.getId()
            );

            model.addAttribute("shop", shop);
            model.addAttribute(
                    "errorMessage",
                    "Thông tin cửa hàng chưa hợp lệ."
            );

            return "vendor/shop-edit";
        }

        shopService.updateShop(
                shopId,
                owner.getId(),
                request
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Cập nhật thông tin cửa hàng thành công."
        );

        return "redirect:/vendor/shops/" + shopId;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElse(null);
    }
}