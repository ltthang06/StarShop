package vn.iotstar.starshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.service.CustomerAuthService;

@Controller
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthService authService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            String registeredEmail = authService.register(
                    fullName,
                    email,
                    password,
                    confirmPassword
            );
            redirectAttributes.addAttribute("email", registeredEmail);
            return "redirect:/verify-otp";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyForm(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verify(
            @RequestParam String email,
            @RequestParam String code,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            authService.activate(email, code);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Tài khoản đã được kích hoạt. Vui lòng đăng nhập."
            );
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("email", email);
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/verify-otp";
        }
    }

    @PostMapping("/register/resend")
    public String resend(@RequestParam String email, Model model) {
        try {
            authService.resendActivation(email);
            model.addAttribute("successMessage", "Mã mới đã được gửi");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }
        model.addAttribute("email", email);
        return "auth/verify-otp";
    }

    @GetMapping("/forgot-password")
    public String forgotForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgot(@RequestParam String email, Model model) {
        try {
            authService.requestPasswordReset(email);
            model.addAttribute(
                    "successMessage",
                    "Nếu email có tài khoản, mã đặt lại mật khẩu đã được gửi."
            );
            model.addAttribute("email", email);
            return "auth/reset-password";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("email", email);
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetForm(
            @RequestParam(required = false) String email,
            Model model) {

        model.addAttribute("email", email);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            authService.resetPassword(
                    email,
                    code,
                    password,
                    confirmPassword
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Mật khẩu đã được cập nhật."
            );
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("email", email);
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/reset-password";
        }
    }
}
