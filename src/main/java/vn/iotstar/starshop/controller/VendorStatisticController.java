package vn.iotstar.starshop.controller;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.VendorStatisticData;
import vn.iotstar.starshop.entity.Shop;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.repository.UserRepository;
import vn.iotstar.starshop.service.ShopService;
import vn.iotstar.starshop.service.VendorStatisticService;

@Controller
@RequestMapping("/vendor/shops/{shopId}/statistics")
@RequiredArgsConstructor
public class VendorStatisticController {

    private static final ZoneId VIETNAM_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private final VendorStatisticService statisticService;

    private final ShopService shopService;

    private final UserRepository userRepository;

    @GetMapping
    public String statistics(
            @PathVariable Long shopId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
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

        VendorStatisticData statistics =
                statisticService.getStatistics(
                        shopId,
                        owner.getId()
                );

        LocalDate today =
                LocalDate.now(VIETNAM_ZONE);

        if (from == null) {
            from =
                    today.withDayOfMonth(1);
        }

        if (to == null) {
            to = today;
        }

        VendorStatisticData.RangeStatistic
                rangeStatistic;

        try {

            rangeStatistic =
                    statisticService
                            .getRangeStatistics(
                                    shopId,
                                    owner.getId(),
                                    from,
                                    to
                            );

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            from =
                    today.withDayOfMonth(1);

            to = today;

            rangeStatistic =
                    statisticService
                            .getRangeStatistics(
                                    shopId,
                                    owner.getId(),
                                    from,
                                    to
                            );
        }

        model.addAttribute(
                "shop",
                shop
        );

        model.addAttribute(
                "statistics",
                statistics
        );

        model.addAttribute(
                "rangeStatistic",
                rangeStatistic
        );

        model.addAttribute(
                "fromDate",
                from
        );

        model.addAttribute(
                "toDate",
                to
        );

        return "vendor/statistics";
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