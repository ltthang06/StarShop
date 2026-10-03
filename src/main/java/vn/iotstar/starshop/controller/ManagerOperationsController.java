package vn.iotstar.starshop.controller;
import java.util.function.Supplier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.ShippingProviderForm;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.ManagerOperationsService;
@Controller @RequiredArgsConstructor @RequestMapping("/manager")
public class ManagerOperationsController {
    private final ManagerOperationsService service;
    private final ShippingProviderRepository providers;
    private final ManagerUserRepository users;
    @GetMapping("/users")
    public String users(@RequestParam(defaultValue="") String keyword, @RequestParam(defaultValue="0") int page, Model model) {
        model.addAttribute("users",service.users(keyword,page)); model.addAttribute("keyword",keyword);
        return "manager/users";
    }
    @PostMapping("/users/{id}/lock")
    public String lock(@PathVariable Long id, @RequestParam boolean locked, Authentication auth, RedirectAttributes redirect) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return action(() -> {service.setUserLocked(id,locked,auth.getName(),admin); return "Đã cập nhật tài khoản";},
            "/manager/users",redirect);
    }
    @GetMapping("/shipping-providers")
    public String providers(@RequestParam(required=false) Long edit, Model model) {
        ShippingProviderForm form = new ShippingProviderForm();
        if (edit != null) {
            var provider = providers.findById(edit).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển"));
            form.setId(provider.getId()); form.setName(provider.getName()); form.setPhone(provider.getPhone());
            form.setBaseFee(provider.getBaseFee()); form.setEstimatedDays(provider.getEstimatedDays()); form.setActive(provider.isActive());
        }
        prepareProviders(model,form); return "manager/shipping-providers";
    }
    @PostMapping("/shipping-providers")
    public String saveProvider(@Valid @ModelAttribute("providerForm") ShippingProviderForm form, BindingResult errors,
        Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try { service.saveProvider(form); redirect.addFlashAttribute("success","Đã lưu đơn vị vận chuyển");
                return "redirect:/manager/shipping-providers";
            } catch (IllegalArgumentException ex) {model.addAttribute("error",ex.getMessage());}
        }
        prepareProviders(model,form); return "manager/shipping-providers";
    }
    private void prepareProviders(Model model, ShippingProviderForm form) {
        model.addAttribute("providerForm",form); model.addAttribute("providers",providers.findAllByOrderByNameAsc());
    }
    @GetMapping("/orders")
    public String orders(@RequestParam(required=false) OrderStatus status, @RequestParam(defaultValue="0") int page, Model model) {
        model.addAttribute("orders",service.orders(status,page)); model.addAttribute("statuses",OrderStatus.values());
        model.addAttribute("selectedStatus",status); model.addAttribute("shippers",users.findAvailable(RoleName.SHIPPER,UserStatus.ACTIVE));
        model.addAttribute("providers",providers.findByActiveTrueOrderByNameAsc());
        return "manager/orders";
    }
    @PostMapping("/orders/{id}/assign")
    public String assign(@PathVariable Long id, @RequestParam Long shipperId, @RequestParam Long providerId,
        @RequestParam(defaultValue="") String note, RedirectAttributes redirect) {
        return action(() -> {service.assign(id,shipperId,providerId,note); return "Đã phân công giao hàng";},
            "/manager/orders",redirect);
    }
    private String action(Supplier<String> work, String path, RedirectAttributes redirect) {
        try {redirect.addFlashAttribute("success",work.get());}
        catch (IllegalArgumentException ex) {redirect.addFlashAttribute("error",ex.getMessage());}
        return "redirect:" + path;
    }
}
