package vn.iotstar.starshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.starshop.service.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/payments")
public class ManagerPaymentController {
    private final VnpayPaymentService payments;
    private final VnpayReconciliationService reconciliation;

    @GetMapping
    public String unresolved(@RequestParam(defaultValue="0") int page,Model model) {
        model.addAttribute("payments",payments.unresolved(page));return "manager/payments";
    }
    @PostMapping("/{reference}/reconcile")
    public String reconcile(@PathVariable String reference,RedirectAttributes flash) {
        try {reconciliation.reconcile(reference,null);flash.addFlashAttribute("success","Đã kiểm tra giao dịch. Xem kết quả và ghi chú bên dưới.");}
        catch(IllegalArgumentException ex) {flash.addFlashAttribute("error",ex.getMessage());}
        return "redirect:/manager/payments";
    }
}
