package vn.iotstar.starshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.iotstar.starshop.dto.ShopRequest;

@Controller
@RequestMapping("/vendor")
public class VendorController {

    @GetMapping
    @ResponseBody
    public String vendorHome() {
        return "Vendor module is running";
    }

    @GetMapping("/shop/register")
    public String showRegisterShopForm(Model model) {
        model.addAttribute("shopRequest", new ShopRequest());
        return "vendor/shop-register";
    }
}