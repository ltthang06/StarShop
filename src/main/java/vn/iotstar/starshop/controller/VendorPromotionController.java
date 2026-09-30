package vn.iotstar.starshop.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

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
import vn.iotstar.starshop.dto.PromotionRequest;
import vn.iotstar.starshop.entity.Promotion;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.PromotionType;
import vn.iotstar.starshop.repository.UserRepository;
import vn.iotstar.starshop.service.PromotionService;
import vn.iotstar.starshop.service.ShopService;

@Controller
@RequestMapping("/vendor/shops/{shopId}/promotions")
@RequiredArgsConstructor
public class VendorPromotionController {

    private static final ZoneId VIETNAM_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final PromotionService promotionService;
    private final ShopService shopService;
    private final UserRepository userRepository;

    @GetMapping
    public String list(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        User owner =
                getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        Shop shop =
                shopService.getShopByOwner(
                        shopId,
                        owner.getId()
                );

        Page<Promotion> promotions =
                promotionService
                        .searchVendorPromotions(
                                shopId,
                                owner.getId(),
                                keyword,
                                active,
                                page,
                                size
                        );

        model.addAttribute(
                "shop",
                shop
        );

        model.addAttribute(
                "promotions",
                promotions
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "selectedActive",
                active
        );

        model.addAttribute(
                "size",
                size
        );

        return "vendor/promotion-list";
    }

    @GetMapping("/create")
    public String showCreateForm(
            @PathVariable Long shopId,
            Authentication authentication,
            Model model) {

        User owner =
                getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        Shop shop =
                shopService.getShopByOwner(
                        shopId,
                        owner.getId()
                );

        PromotionRequest request =
                new PromotionRequest();

        LocalDateTime nowVietnam =
                LocalDateTime.now(
                        VIETNAM_ZONE
                )
                .truncatedTo(
                        ChronoUnit.MINUTES
                );

        request.setStartAt(
                nowVietnam
        );

        request.setActive(true);
        request.setQuantity(100);

        prepareForm(
                model,
                shop,
                request,
                false,
                null
        );

        return "vendor/promotion-form";
    }

    @PostMapping("/create")
    public String create(
            @PathVariable Long shopId,
            @Valid
            @ModelAttribute("promotionRequest")
            PromotionRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        User owner =
                getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        Shop shop =
                shopService.getShopByOwner(
                        shopId,
                        owner.getId()
                );

        if (bindingResult.hasErrors()) {

            prepareForm(
                    model,
                    shop,
                    request,
                    false,
                    null
            );

            return "vendor/promotion-form";
        }

        try {

            promotionService
                    .createPromotion(
                            shopId,
                            owner.getId(),
                            request
                    );

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            prepareForm(
                    model,
                    shop,
                    request,
                    false,
                    null
            );

            return "vendor/promotion-form";
        }

        redirectAttributes
                .addFlashAttribute(
                        "successMessage",
                        "Thêm khuyến mãi thành công."
                );

        return "redirect:/vendor/shops/"
                + shopId
                + "/promotions";
    }

    @GetMapping("/{promotionId}/edit")
    public String showEditForm(
            @PathVariable Long shopId,
            @PathVariable Long promotionId,
            Authentication authentication,
            Model model) {

        User owner =
                getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        Shop shop =
                shopService.getShopByOwner(
                        shopId,
                        owner.getId()
                );

        Promotion promotion =
                promotionService
                        .getPromotionByOwner(
                                promotionId,
                                shopId,
                                owner.getId()
                        );

        PromotionRequest request =
                new PromotionRequest();

        request.setCode(
                promotion.getCode()
        );

        request.setName(
                promotion.getName()
        );

        request.setDescription(
                promotion.getDescription()
        );

        request.setType(
                promotion.getType()
        );

        request.setDiscountValue(
                promotion.getDiscountValue()
        );

        request.setMinOrderAmount(
                promotion.getMinOrderAmount()
        );

        request.setMaxDiscountAmount(
                promotion.getMaxDiscountAmount()
        );

        request.setQuantity(
                promotion.getQuantity()
        );

        request.setStartAt(
                promotion.getStartAt()
        );

        request.setEndAt(
                promotion.getEndAt()
        );

        request.setActive(
                promotion.isActive()
        );

        prepareForm(
                model,
                shop,
                request,
                true,
                promotionId
        );

        return "vendor/promotion-form";
    }

    @PostMapping("/{promotionId}/edit")
    public String update(
            @PathVariable Long shopId,
            @PathVariable Long promotionId,
            @Valid
            @ModelAttribute("promotionRequest")
            PromotionRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        User owner =
                getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        Shop shop =
                shopService.getShopByOwner(
                        shopId,
                        owner.getId()
                );

        if (bindingResult.hasErrors()) {

            prepareForm(
                    model,
                    shop,
                    request,
                    true,
                    promotionId
            );

            return "vendor/promotion-form";
        }

        try {

            promotionService
                    .updatePromotion(
                            promotionId,
                            shopId,
                            owner.getId(),
                            request
                    );

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            prepareForm(
                    model,
                    shop,
                    request,
                    true,
                    promotionId
            );

            return "vendor/promotion-form";
        }

        redirectAttributes
                .addFlashAttribute(
                        "successMessage",
                        "Cập nhật khuyến mãi thành công."
                );

        return "redirect:/vendor/shops/"
                + shopId
                + "/promotions";
    }

    @PostMapping("/{promotionId}/active")
    public String changeActive(
            @PathVariable Long shopId,
            @PathVariable Long promotionId,
            @RequestParam boolean active,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User owner =
                getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        promotionService.setActive(
                promotionId,
                shopId,
                owner.getId(),
                active
        );

        redirectAttributes
                .addFlashAttribute(
                        "successMessage",
                        active
                                ? "Đã bật khuyến mãi."
                                : "Đã tắt khuyến mãi."
                );

        return "redirect:/vendor/shops/"
                + shopId
                + "/promotions";
    }

    private void prepareForm(
            Model model,
            Shop shop,
            PromotionRequest request,
            boolean editing,
            Long promotionId) {

        model.addAttribute(
                "shop",
                shop
        );

        model.addAttribute(
                "promotionRequest",
                request
        );

        model.addAttribute(
                "types",
                PromotionType.values()
        );

        model.addAttribute(
                "editing",
                editing
        );

        model.addAttribute(
                "promotionId",
                promotionId
        );
    }

    private User getCurrentUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication
                .isAuthenticated()) {

            return null;
        }

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElse(null);
    }
}