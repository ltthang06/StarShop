package vn.iotstar.starshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.PublicShopData;
import vn.iotstar.starshop.service.PublicShopService;

@Controller
@RequiredArgsConstructor
public class PublicShopController {

    private final PublicShopService publicShopService;

    @GetMapping("/shop/{id}")
    public String showPublicShop(
            @PathVariable("id") Long shopId,
            @RequestParam(defaultValue = "")
            String keyword,
            @RequestParam(required = false)
            Long categoryId,
            @RequestParam(defaultValue = "0")
            int page,
            Model model) {

        try {

            PublicShopData shopData =
                    publicShopService
                            .getPublicShop(
                                    shopId,
                                    keyword,
                                    categoryId,
                                    page,
                                    12
                            );

            model.addAttribute(
                    "shopData",
                    shopData
            );

            model.addAttribute(
                    "keyword",
                    keyword
            );

            model.addAttribute(
                    "categoryId",
                    categoryId
            );

            return "public/shop-public";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "public/shop-public";
        }
    }
}