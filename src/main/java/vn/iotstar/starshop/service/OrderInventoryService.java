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
        restoreMany(java.util.List.of(orderId));
    }
    @Transactional
    public void restoreMany(java.util.List<Long> orderIds) {
        var quantities=new java.util.TreeMap<Long,Integer>();
        orderIds.forEach(id -> details.inventoryLines(id).forEach(line -> quantities.merge(line.getProductId(),line.getQuantity(),Integer::sum)));
        quantities.forEach((id,quantity) -> {
                var product=products.findForModeration(id).orElseThrow();
                product.setQuantity(product.getQuantity()+quantity);
                product.setSoldCount(Math.max(0,product.getSoldCount()-quantity));
                if (product.getStatus()==ProductStatus.OUT_OF_STOCK && !moderations.existsByProductIdAndBlockedTrue(product.getId()))
                    product.setStatus(ProductStatus.ACTIVE);
        });
    }
}
