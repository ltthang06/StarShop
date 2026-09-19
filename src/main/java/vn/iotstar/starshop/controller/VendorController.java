package vn.iotstar.starshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/vendor")
public class VendorController {

    @GetMapping
    @ResponseBody
    public String vendorHome() {
        return "Vendor module is running";
    }
}