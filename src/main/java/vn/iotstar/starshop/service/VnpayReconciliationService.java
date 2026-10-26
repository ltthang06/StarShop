package vn.iotstar.starshop.service;

import java.time.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.iotstar.starshop.config.VnpayProperties;
import vn.iotstar.starshop.repository.VnpayPaymentRepository;

@Service
@RequiredArgsConstructor
public class VnpayReconciliationService {
    private final VnpayPaymentService payments;
    private final VnpayPaymentRepository rows;
    private final VnpayQueryGateway gateway;
    private final VnpayProperties config;

    public void reconcile(String reference,String email) {
        if(!config.ready()) return;
        var target=payments.prepareReconciliation(reference,email);
        if(target==null) return;
        try {payments.applyQueryResult(gateway.query(target));}
        catch(RuntimeException ex) {payments.reconciliationUnavailable(reference);}
    }

    public void reconcileDue() {
        if(!config.ready()) return;
        var now=LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        for(String ref:rows.findDue(vn.iotstar.starshop.entity.VnpayPayment.State.PENDING,now,now.minusMinutes(5),PageRequest.of(0,10))) {
            try {reconcile(ref,null);} catch(RuntimeException ignored) { }
        }
    }
}
