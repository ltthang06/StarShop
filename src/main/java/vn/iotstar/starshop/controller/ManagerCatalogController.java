package vn.iotstar.starshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import vn.iotstar.starshop.service.ManagerCatalogService;
import vn.iotstar.starshop.dto.PromotionRequest;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.ProductModerationRepository;

@Controller @RequiredArgsConstructor @RequestMapping("/manager")
public class ManagerCatalogController {
    private final ManagerCatalogService service;
    private final ProductModerationRepository moderations;

    @GetMapping("/products")
    public String products(@RequestParam(defaultValue="") String keyword, @RequestParam(required=false) Long shopId,
        @RequestParam(required=false) ProductStatus status, @RequestParam(defaultValue="0") int page, Model model) {
        var result=service.products(keyword,shopId,status,page);
        model.addAttribute("products",result);
        model.addAttribute("keyword",keyword); model.addAttribute("shopId",shopId);
        model.addAttribute("selectedStatus",status); model.addAttribute("statuses",ProductStatus.values());
        model.addAttribute("moderations",moderations.findAllById(result.stream().map(p -> p.getId()).toList()).stream().collect(
            java.util.stream.Collectors.toMap(m -> m.getProductId(), m -> m)));
        return "manager/products";
    }
    @PostMapping("/products/{id}/moderate")
    public String moderate(@PathVariable Long id, @RequestParam boolean blocked,
        @RequestParam(defaultValue="") String reason, Principal principal, RedirectAttributes redirect) {
        try { service.moderate(id,blocked,reason,principal.getName()); redirect.addFlashAttribute("success","Đã cập nhật kiểm duyệt"); }
        catch (IllegalArgumentException ex) {redirect.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/manager/products";
    }
    @GetMapping("/promotions")
    public String promotions(@RequestParam(defaultValue="") String keyword, @RequestParam(defaultValue="0") int page, Model model) {
        model.addAttribute("promotions",service.promotions(keyword,page)); model.addAttribute("keyword",keyword);
        return "manager/promotions";
    }
    @GetMapping("/promotions/new")
    public String newPromotion(Model model) {
        model.addAttribute("promotionForm",new PromotionRequest()); prepare(model,null); return "manager/promotion-form";
    }
    @GetMapping("/promotions/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            var p=service.systemPromotion(id); var form=new PromotionRequest();
            form.setCode(p.getCode()); form.setName(p.getName()); form.setDescription(p.getDescription());
            form.setType(p.getType()); form.setDiscountValue(p.getDiscountValue());
            form.setMinOrderAmount(p.getMinOrderAmount()); form.setMaxDiscountAmount(p.getMaxDiscountAmount());
            form.setQuantity(p.getQuantity()); form.setStartAt(p.getStartAt()); form.setEndAt(p.getEndAt()); form.setActive(p.isActive());
            model.addAttribute("promotionForm",form); prepare(model,id); return "manager/promotion-form";
        } catch (IllegalArgumentException ex) {redirect.addFlashAttribute("error",ex.getMessage());return "redirect:/manager/promotions";}
    }
    @PostMapping("/promotions/save")
    public String save(@RequestParam(required=false) Long id, @Valid @ModelAttribute("promotionForm") PromotionRequest form,
        BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {service.savePromotion(id,form);redirect.addFlashAttribute("success","Đã lưu voucher hệ thống");return "redirect:/manager/promotions";}
            catch (IllegalArgumentException ex) {model.addAttribute("error",ex.getMessage());}
        }
        prepare(model,id);return "manager/promotion-form";
    }
    @PostMapping("/promotions/{id}/active")
    public String toggle(@PathVariable Long id, @RequestParam boolean active, RedirectAttributes redirect) {
        try {service.togglePromotion(id,active);redirect.addFlashAttribute("success","Đã cập nhật voucher");}
        catch (IllegalArgumentException ex) {redirect.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/manager/promotions";
    }
    private void prepare(Model model, Long id) {model.addAttribute("promotionId",id);model.addAttribute("types",PromotionType.values());}
}
