package vn.iotstar.starshop.controller;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.starshop.service.*;

@Controller @RequiredArgsConstructor @RequestMapping("/payments")
public class VnpayController {
    private final VnpayPaymentService payments;
    private final VnpaySigner signer;
    private final VnpayReconciliationService reconciliation;

    @GetMapping("/vnpay/ipn") @ResponseBody
    public ResponseEntity<Map<String,String>> ipn(HttpServletRequest request) {
        Map<String,String> result;
        try {result=payments.ipn(request.getParameterMap());}
        catch(RuntimeException ex) {result=Map.of("RspCode","99","Message","Unable to confirm");}
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    @GetMapping("/vnpay/return")
    public String returned(HttpServletRequest request,Authentication auth,Model model) {
        try {
            var fields=signer.verified(request.getParameterMap());
            payments.handleReturn(request.getParameterMap(),auth.getName());
            var view=payments.view(fields.get("vnp_TxnRef"),auth.getName());
            model.addAllAttributes(view);
            model.addAttribute("returnNotice","Đã kiểm tra kết quả do VNPAY gửi về. Trạng thái giao dịch được hiển thị bên trên.");
        } catch(IllegalArgumentException ex) {model.addAttribute("errorMessage","Không xác minh được kết quả thanh toán. Hãy kiểm tra giao dịch trong lịch sử đơn hàng.");}
        return "customer/payment";
    }

    @GetMapping("/{reference}")
    public String view(@PathVariable String reference,Authentication auth,Model model) {
        try {model.addAllAttributes(payments.view(reference,auth.getName()));}
        catch(IllegalArgumentException ex) {throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy giao dịch");}
        return "customer/payment";
    }

    @GetMapping("/{reference}/status") @ResponseBody
    public ResponseEntity<Map<String,String>> status(@PathVariable String reference,Authentication auth) {
        try {
            var view=payments.view(reference,auth.getName());
            return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                    .body(Map.of("state",view.get("paymentState").toString()));
        } catch(IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).cacheControl(CacheControl.noStore()).body(Map.of("state","NOT_FOUND"));
        }
    }

    @PostMapping("/{reference}/reconcile")
    public String reconcile(@PathVariable String reference,Authentication auth,RedirectAttributes flash) {
        try {reconciliation.reconcile(reference,auth.getName());flash.addFlashAttribute("checkNotice","Đã kiểm tra trạng thái. Nếu chưa có kết quả cuối cùng, vui lòng chờ hoặc liên hệ cửa hàng.");}
        catch(IllegalArgumentException ex) {flash.addFlashAttribute("errorMessage",ex.getMessage());}
        return "redirect:/payments/"+reference;
    }

    @GetMapping("/{reference}/redirect")
    public String redirect(@PathVariable String reference,Authentication auth,RedirectAttributes flash) {
        String returnUrl=org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/payments/vnpay/return").build().toUriString();
        try {return "redirect:"+payments.redirectUrl(reference,auth.getName(),returnUrl);}
        catch(IllegalArgumentException ex) {flash.addFlashAttribute("errorMessage",ex.getMessage());return "redirect:/orders";}
    }

    @GetMapping("/orders/{id}")
    public String order(@PathVariable Long id,Authentication auth) {
        try {return "redirect:/payments/"+payments.referenceForOrder(id,auth.getName());}
        catch(IllegalArgumentException ex) {throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy giao dịch");}
    }
}
