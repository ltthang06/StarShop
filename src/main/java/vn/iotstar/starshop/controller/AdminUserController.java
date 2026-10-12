package vn.iotstar.starshop.controller;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.starshop.repository.ManagerUserRepository;
import vn.iotstar.starshop.service.AdminUserService;
import vn.iotstar.starshop.enums.RoleName;
@Controller @RequiredArgsConstructor
public class AdminUserController {
    private final ManagerUserRepository users;
    private final AdminUserService service;
    @GetMapping("/manager/users/{id}")
    public String detail(@PathVariable Long id,Model model,RedirectAttributes redirect) {
        var user=users.findById(id);
        if(user.isEmpty()) {redirect.addFlashAttribute("error","Không tìm thấy tài khoản");return "redirect:/manager/users";}
        model.addAttribute("account",user.get());return "manager/user-detail";
    }
    @PostMapping("/manager/users/{id}/roles")
    public String roles(@PathVariable Long id,@RequestParam RoleName role,@RequestParam boolean enabled,
        Principal principal,RedirectAttributes redirect) {
        try {service.setOperationalRole(id,role,enabled,principal.getName());redirect.addFlashAttribute("success","Đã cập nhật quyền");}
        catch(IllegalArgumentException ex) {redirect.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/manager/users/"+id;
    }
}
