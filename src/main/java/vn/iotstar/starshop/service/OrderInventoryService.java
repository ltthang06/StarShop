package vn.iotstar.starshop.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.enums.ProductStatus;
@Service @RequiredArgsConstructor
public class OrderInventoryService {
    private final OrderDetailRepository details;
    private final ProductRepository products;
    private final ProductModerationRepository moderations;
    @Transactional
    public void restore(Long orderId) {
        details.findByOrderIdOrderByIdAsc(orderId).stream()
            .sorted(java.util.Comparator.comparing(d -> d.getProduct().getId()))
            .forEach(detail -> {
                var product=products.findForModeration(detail.getProduct().getId()).orElseThrow();
                product.setQuantity(product.getQuantity()+detail.getQuantity());
                product.setSoldCount(Math.max(0,product.getSoldCount()-detail.getQuantity()));
                if (product.getStatus()==ProductStatus.OUT_OF_STOCK && !moderations.existsByProductIdAndBlockedTrue(product.getId()))
                    product.setStatus(ProductStatus.ACTIVE);
            });
    }
}
