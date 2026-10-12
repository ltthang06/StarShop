package vn.iotstar.starshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.service.CustomerCatalogService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CustomerCatalogService catalogService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts", catalogService.featured());
        return "guest/home";
    }
}
