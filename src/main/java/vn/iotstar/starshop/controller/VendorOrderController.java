package vn.iotstar.starshop.controller;

import org.springframework.data.domain.Page;
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
import vn.iotstar.starshop.entity.Order;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.repository.UserRepository;
import vn.iotstar.starshop.service.OrderService;
import vn.iotstar.starshop.service.ShopService;

@Controller
@RequestMapping("/vendor/shops/{shopId}/orders")
@RequiredArgsConstructor
public class VendorOrderController {

    private final OrderService orderService;
    private final ShopService shopService;
    private final UserRepository userRepository;

    @GetMapping
    public String showOrders(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "")
            String keyword,
            @RequestParam(defaultValue = "")
            String status,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "10")
            int size,
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
                        owner.getId());

        OrderStatus statusValue =
                parseStatus(status);

        Page<Order> orders =
                orderService.searchVendorOrders(
                        shopId,
                        owner.getId(),
                        keyword,
                        statusValue,
                        page,
                        size);

        model.addAttribute(
                "shop",
                shop);

        model.addAttribute(
                "orders",
                orders);

        model.addAttribute(
                "statuses",
                OrderStatus.values());

        model.addAttribute(
                "keyword",
                keyword);

        model.addAttribute(
                "selectedStatus",
                statusValue);

        model.addAttribute(
                "size",
                size);

        return "vendor/order-list";
    }

    @GetMapping("/{orderId}")
    public String showOrderDetail(
            @PathVariable Long shopId,
            @PathVariable Long orderId,
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
                        owner.getId());

        Order order =
                orderService.getOrderByOwner(
                        orderId,
                        shopId,
                        owner.getId());

        model.addAttribute(
                "shop",
                shop);

        model.addAttribute(
                "order",
                order);

        model.addAttribute(
                "orderDetails",
                orderService.getOrderDetails(
                        orderId));

        return "vendor/order-detail";
    }

    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(
            @PathVariable Long shopId,
            @PathVariable Long orderId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User owner =
                getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        try {

            orderService.confirmOrder(
                    orderId,
                    shopId,
                    owner.getId());

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "Xác nhận đơn hàng thành công.");

        } catch (IllegalArgumentException e) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            e.getMessage());
        }

        return redirectToDetail(
                shopId,
                orderId);
    }

    @PostMapping("/{orderId}/ready")
    public String readyForPickup(
            @PathVariable Long shopId,
            @PathVariable Long orderId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User owner =
                getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        try {

            orderService.markReadyForPickup(
                    orderId,
                    shopId,
                    owner.getId());

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "Đơn hàng đã sẵn sàng để lấy.");

        } catch (IllegalArgumentException e) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            e.getMessage());
        }

        return redirectToDetail(
                shopId,
                orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(
            @PathVariable Long shopId,
            @PathVariable Long orderId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User owner =
                getCurrentUser(authentication);

        if (owner == null) {
            return "redirect:/vendor";
        }

        try {

            orderService.cancelOrder(
                    orderId,
                    shopId,
                    owner.getId());

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "Đã hủy đơn hàng.");

        } catch (IllegalArgumentException e) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            e.getMessage());
        }

        return redirectToDetail(
                shopId,
                orderId);
    }

    private String redirectToDetail(
            Long shopId,
            Long orderId) {

        return "redirect:/vendor/shops/"
                + shopId
                + "/orders/"
                + orderId;
    }

    private OrderStatus parseStatus(
            String status) {

        if (status == null
                || status.isBlank()) {
            return null;
        }

        try {

            return OrderStatus.valueOf(
                    status);

        } catch (IllegalArgumentException e) {

            return null;
        }
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
                        authentication.getName())
                .orElse(null);
    }
}