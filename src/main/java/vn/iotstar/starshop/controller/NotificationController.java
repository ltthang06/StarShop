package vn.iotstar.starshop.controller;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.starshop.service.NotificationService;
@Controller @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;
    @GetMapping("/notifications")
    public String list(Principal principal,@RequestParam(defaultValue="0") int page,Model model) {
        model.addAttribute("notifications",service.list(principal.getName(),page));return "notifications";
    }
    @GetMapping("/notifications/unread") @ResponseBody
    public Map<String,Long> unread(Principal principal) {return Map.of("count",service.unread(principal.getName()));}
    @PostMapping("/notifications/{id}/read")
    public String read(@PathVariable Long id,Principal principal,RedirectAttributes redirect) {
        try {service.markRead(id,principal.getName());}
        catch (IllegalArgumentException ex) {redirect.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/notifications";
    }
}
