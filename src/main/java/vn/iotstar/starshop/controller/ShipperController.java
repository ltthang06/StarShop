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
import vn.iotstar.starshop.entity.ShipperAssignment;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.OrderStatus;
import vn.iotstar.starshop.repository.UserRepository;
import vn.iotstar.starshop.service.ShipperService;

@Controller
@RequestMapping("/shipper")
@RequiredArgsConstructor
public class ShipperController {

    private final ShipperService shipperService;
    private final UserRepository userRepository;

    @GetMapping
    public String dashboard(
            Authentication authentication) {

        User shipper = getCurrentUser(authentication);

        if (shipper == null) {
            return "redirect:/";
        }

        return "redirect:/shipper/orders";
    }

    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        User shipper = getCurrentUser(authentication);

        if (shipper == null) {
            return "redirect:/";
        }

        Page<ShipperAssignment> assignments =
                shipperService.getAssignments(
                        shipper.getId(),
                        page,
                        size
                );

        model.addAttribute(
                "shipper",
                shipper
        );

        model.addAttribute(
                "assignments",
                assignments
        );

        model.addAttribute(
                "totalAssignments",
                shipperService.countAll(
                        shipper.getId()
                )
        );

        model.addAttribute(
                "assignedCount",
                shipperService.countByStatus(
                        shipper.getId(),
                        OrderStatus.ASSIGNED
                )
        );

        model.addAttribute(
                "shippingCount",
                shipperService.countByStatus(
                        shipper.getId(),
                        OrderStatus.SHIPPING
                )
        );

        model.addAttribute(
                "deliveredCount",
                shipperService.countByStatus(
                        shipper.getId(),
                        OrderStatus.DELIVERED
                )
        );

        return "shipper/order-list";
    }

    @GetMapping("/orders/{assignmentId}")
    public String detail(
            @PathVariable Long assignmentId,
            Authentication authentication,
            Model model) {

        User shipper = getCurrentUser(authentication);

        if (shipper == null) {
            return "redirect:/";
        }

        ShipperAssignment assignment =
                shipperService.getAssignment(
                        assignmentId,
                        shipper.getId()
                );

        model.addAttribute(
                "assignment",
                assignment
        );

        model.addAttribute(
                "order",
                assignment.getOrder()
        );

        return "shipper/order-detail";
    }

    @PostMapping("/orders/{assignmentId}/status")
    public String updateStatus(
            @PathVariable Long assignmentId,
            @RequestParam OrderStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User shipper = getCurrentUser(authentication);

        if (shipper == null) {
            return "redirect:/";
        }

        try {

            shipperService.updateOrderStatus(
                    assignmentId,
                    shipper.getId(),
                    status
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật trạng thái giao hàng thành công."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/shipper/orders/"
                + assignmentId;
    }

    private User getCurrentUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return null;
        }

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElse(null);
    }
}