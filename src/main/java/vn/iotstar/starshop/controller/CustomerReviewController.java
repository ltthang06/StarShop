package vn.iotstar.starshop.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.service.CustomerReviewService;

@Controller
@RequiredArgsConstructor
public class CustomerReviewController {

    private final CustomerReviewService reviewService;

    @GetMapping("/orders/{orderId}/items/{detailId}/review")
    public String form(
            Authentication authentication,
            @PathVariable Long orderId,
            @PathVariable Long detailId,
            Model model) {

        try {
            model.addAttribute("target", reviewService.target(
                    authentication.getName(), orderId, detailId));
            return "customer/review-form";
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PostMapping("/orders/{orderId}/items/{detailId}/review")
    public String submit(
            Authentication authentication,
            @PathVariable Long orderId,
            @PathVariable Long detailId,
            @RequestParam int rating,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) MultipartFile video,
            RedirectAttributes redirectAttributes) {

        try {
            reviewService.submit(
                    authentication.getName(), orderId, detailId,
                    rating, content, image, video);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Đã gửi đánh giá");
            return "redirect:/orders";
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Không thể tải tệp lên");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + orderId
                + "/items/" + detailId + "/review";
    }
}
