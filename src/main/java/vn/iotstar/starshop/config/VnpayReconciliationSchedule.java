package vn.iotstar.starshop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import vn.iotstar.starshop.service.VnpayReconciliationService;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name="starshop.vnpay.reconciliation-enabled",havingValue="true")
public class VnpayReconciliationSchedule {
    private final VnpayReconciliationService reconciliation;
    @Scheduled(fixedDelay=60000,initialDelay=60000)
    public void checkPendingPayments() {reconciliation.reconcileDue();}
}
