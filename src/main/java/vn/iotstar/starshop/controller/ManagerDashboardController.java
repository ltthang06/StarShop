package vn.iotstar.starshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.service.CategoryService;
import vn.iotstar.starshop.service.ManagerShopService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerDashboardController {

    private final CategoryService categoryService;
    private final ManagerShopService shopService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("categoryCount", categoryService.count());
        model.addAttribute("pendingShopCount", shopService.countPending());
        return "manager/dashboard";
    }
}
