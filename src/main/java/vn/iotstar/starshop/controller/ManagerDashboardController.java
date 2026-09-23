package vn.iotstar.starshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.service.CategoryService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerDashboardController {

    private final CategoryService categoryService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("categoryCount", categoryService.count());
        return "manager/dashboard";
    }
}
