package vn.iotstar.starshop.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.enums.ShopStatus;
import vn.iotstar.starshop.service.ManagerShopService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/shops")
public class ManagerShopController {

    private final ManagerShopService shopService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) ShopStatus status,
            @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("shops", shopService.search(keyword, status, page));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ShopStatus.values());
        return "manager/shops";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("shop", shopService.findById(id));
        model.addAttribute("commission", shopService.currentCommission(id).orElse(null));
        return "manager/shop-detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirect) {
        return changeStatus(id, redirect, () -> shopService.approve(id), "Đã duyệt cửa hàng");
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirect) {
        return changeStatus(id, redirect, () -> shopService.reject(id), "Đã từ chối cửa hàng");
    }

    @PostMapping("/{id}/block")
    public String block(@PathVariable Long id, RedirectAttributes redirect) {
        return changeStatus(id, redirect, () -> shopService.block(id), "Đã khóa cửa hàng");
    }

    @PostMapping("/{id}/reopen")
    public String reopen(@PathVariable Long id, RedirectAttributes redirect) {
        return changeStatus(id, redirect, () -> shopService.reopen(id), "Đã mở lại cửa hàng");
    }

    @PostMapping("/{id}/commission")
    public String setCommission(@PathVariable Long id, @RequestParam BigDecimal ratePercent,
            RedirectAttributes redirect) {
        return changeStatus(id, redirect, () -> shopService.setCommission(id, ratePercent),
                "Đã cập nhật chiết khấu");
    }

    private String changeStatus(Long id, RedirectAttributes redirect, Runnable action, String success) {
        try {
            action.run();
            redirect.addFlashAttribute("success", success);
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/manager/shops/" + id;
    }
}
