package vn.iotstar.starshop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.config.VnpayProperties;
import vn.iotstar.starshop.enums.PaymentMethod;

@Service @RequiredArgsConstructor
public class OnlineCheckoutService {
    private final VnpayProperties config;
    private final CustomerCheckoutService checkout;
    private final VnpayPaymentService payments;
    @Transactional
    public String place(String email,Long addressId,Long providerId,String note,String code,String ip) {
        config.requireReady();
        var ids=checkout.placeOrder(email,addressId,providerId,note,code,PaymentMethod.VNPAY);
        return payments.create(email,ids,ip);
    }
}
