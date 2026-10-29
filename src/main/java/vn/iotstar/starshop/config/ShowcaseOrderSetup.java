package vn.iotstar.starshop.config;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.*;

@Component @Profile("demo") @ConditionalOnProperty(name="starshop.showcase.orders-enabled",havingValue="true")
@org.springframework.core.annotation.Order(20) @RequiredArgsConstructor
public class ShowcaseOrderSetup implements ApplicationRunner {
    private final UserRepository users;
    private final ShopRepository shops;
    private final ProductRepository products;
    private final OrderRepository orders;
    private final OrderDetailRepository details;
    private final CustomerAddressRepository addresses;
    private final ShippingProviderRepository providers;
    private final ShipperAssignmentRepository assignments;
    private final CustomerReviewRepository reviews;
    private final CustomerCartService cart;
    private final CustomerCheckoutService checkout;
    private final CustomerOrderService history;
    private final OrderService vendor;
    private final ManagerOperationsService manager;
    private final ShipperService delivery;
    private final CustomerReviewService reviewService;
    private final CustomerActivityService activity;
    private final CustomerPromotionService promotionService;

    @Override public void run(ApplicationArguments args) throws Exception {
        var buyer=users.findByEmail("customer@starshop.local").orElseThrow();
        var address=addresses.findByUserIdOrderByDefaultAddressDescIdAsc(buyer.getId()).get(0);
        var provider=providers.findAll().stream().filter(p -> "StarShop Express".equals(p.getName())).findFirst().orElseThrow();
        var shipper=users.findByEmail("shipper@starshop.local").orElseThrow();
        var branchShops=shops.findAll().stream().filter(s -> s.getName().equals("StarShop Võ Văn Ngân") || s.getName().equals("StarShop Nguyễn Gia Trí"))
                .sorted(Comparator.comparing(Shop::getId)).toList();
        if(branchShops.size()!=2) throw new IllegalStateException("Cần hai cửa hàng chi nhánh");
        var productList=products.findAll().stream().sorted(Comparator.comparing(Product::getId)).toList();
        var selection=branchShops.stream().map(shop -> productList.stream().filter(p -> p.getShop().getId().equals(shop.getId()) && p.getQuantity()>10).findFirst().orElseThrow()).toList();
        String[] notes={"Giao hoa sinh nhật buổi sáng","Giao hoa chúc mừng tân gia","Giao hoa lễ khai trương","Giao hoa tặng đồng nghiệp",
                "Giao hoa tặng người thân","Giao hoa kỷ niệm ngày cưới","Giao hoa chúc mừng thăng chức","Giao hoa đổi lịch nhận","Giao hoa đổi mẫu thiết kế","Giao hoa hoàn trả cửa hàng","Giao hoa hoàn tất hoàn tiền"};
        OrderStatus[] states={OrderStatus.NEW,OrderStatus.CONFIRMED,OrderStatus.READY_FOR_PICKUP,OrderStatus.ASSIGNED,OrderStatus.PICKED_UP,
                OrderStatus.SHIPPING,OrderStatus.DELIVERED,OrderStatus.CANCELLED,OrderStatus.RETURN_REQUESTED,OrderStatus.RETURNED,OrderStatus.REFUNDED};
        try {
            for(int i=0;i<states.length;i++) {
                String note=notes[i];
                var product=selection.get(i%selection.size());var shop=branchShops.get(i%branchShops.size());
                var matches=orders.findAll().stream().filter(o -> o.getUser().getId().equals(buyer.getId()) && note.equals(o.getNote())).toList();
                Long orderId;
                if(matches.isEmpty()) {
                    login(buyer.getEmail(),"USER");
                    if(!cart.summary(buyer.getEmail()).getLines().isEmpty()) throw new IllegalStateException("Giỏ hàng tài khoản vận hành đang có sản phẩm");
                    cart.add(buyer.getEmail(),product.getId(),1);
                    orderId=checkout.placeOrder(buyer.getEmail(),address.getId(),provider.getId(),notes[i],i%2==0 ? "HOATUOI10" : "FREESHIP").get(0);
                } else orderId=matches.get(0).getId();
                advance(orderId,states[i],shop,buyer,shipper,provider.getId());
                login(buyer.getEmail(),"USER");activity.recordView(buyer.getEmail(),product.getId());activity.addToWishlist(buyer.getEmail(),product.getId());
                if(states[i]==OrderStatus.DELIVERED && status(orderId)==OrderStatus.DELIVERED) {
                    var line=details.findByOrderIdOrderByIdAsc(orderId).get(0);
                    if(!reviews.existsByOrderDetailId(line.getId())) reviewService.submit(buyer.getEmail(),orderId,line.getId(),5,
                            "Hoa tươi, thiết kế đẹp, đóng gói cẩn thận và giao hàng đúng hẹn. Tôi rất hài lòng với dịch vụ của cửa hàng.",null,null);
                }
            }
            String multiNote="Giao hoa chúc mừng hai cửa hàng";
            var multi=orders.findAll().stream().filter(o -> o.getUser().getId().equals(buyer.getId()) && multiNote.equals(o.getNote())).map(o -> o.getId()).toList();
            if(multi.isEmpty()) {
                login(buyer.getEmail(),"USER");
                if(!cart.summary(buyer.getEmail()).getLines().isEmpty()) throw new IllegalStateException("Giỏ hàng tài khoản vận hành đang có sản phẩm");
                for(var product:selection) cart.add(buyer.getEmail(),product.getId(),1);
                var quote=promotionService.quote(buyer.getEmail(),provider.getId(),"HOATUOI10");
                multi=checkout.placeOrder(buyer.getEmail(),address.getId(),provider.getId(),multiNote,"HOATUOI10");
                var total=multi.stream().map(id -> orders.findById(id).orElseThrow().getTotalAmount()).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add);
                if(multi.size()!=2 || total.compareTo(quote.getTotalAmount())!=0) throw new IllegalStateException("Tách đơn hoặc tổng tiền không khớp báo giá");
            }
            for(int i=0;i<multi.size();i++) {
                Long id=multi.get(i);Long shopId=orders.findById(id).orElseThrow().getShop().getId();
                var shop=branchShops.stream().filter(s -> s.getId().equals(shopId)).findFirst().orElseThrow();
                advance(id,i==0 ? OrderStatus.DELIVERED : OrderStatus.REFUNDED,shop,buyer,shipper,provider.getId());
            }
            System.out.println("STARSHOP_SHOWCASE_ORDERS_READY");
        } finally {SecurityContextHolder.clearContext();}
    }

    private OrderStatus status(Long id) {return orders.findById(id).orElseThrow().getStatus();}
    private void advance(Long id,OrderStatus target,Shop shop,User buyer,User shipper,Long providerId) {
        if(status(id)==target) return;
        if(target==OrderStatus.CANCELLED) {
            if(status(id)==OrderStatus.NEW || status(id)==OrderStatus.CONFIRMED) {login(buyer.getEmail(),"USER");history.cancel(buyer.getEmail(),id);}
            return;
        }
        if(target==OrderStatus.NEW) return;
        login(shop.getName().equals("StarShop Võ Văn Ngân") ? "vendor.vovanngan@starshop.local" : "vendor.nguyengiati@starshop.local","VENDOR");
        if(status(id)==OrderStatus.NEW) vendor.confirmOrder(id,shop.getId(),shop.getOwner().getId());
        if(target==OrderStatus.CONFIRMED) return;
        if(status(id)==OrderStatus.CONFIRMED) vendor.markReadyForPickup(id,shop.getId(),shop.getOwner().getId());
        if(target==OrderStatus.READY_FOR_PICKUP) return;
        login(shop.getName().equals("StarShop Võ Văn Ngân") ? "manager1@starshop.local" : "manager2@starshop.local","MANAGER");
        if(status(id)==OrderStatus.READY_FOR_PICKUP) manager.assign(id,shipper.getId(),providerId,"");
        if(target==OrderStatus.ASSIGNED) return;
        Long assignmentId=assignments.findByOrder_Id(id).orElseThrow().getId();
        login(shipper.getEmail(),"SHIPPER");
        if(status(id)==OrderStatus.ASSIGNED) delivery.updateOrderStatus(assignmentId,shipper.getId(),OrderStatus.PICKED_UP);
        if(target==OrderStatus.PICKED_UP) return;
        if(status(id)==OrderStatus.PICKED_UP) delivery.updateOrderStatus(assignmentId,shipper.getId(),OrderStatus.SHIPPING);
        if(target==OrderStatus.SHIPPING) return;
        if(status(id)==OrderStatus.SHIPPING) delivery.updateOrderStatus(assignmentId,shipper.getId(),OrderStatus.DELIVERED);
        if(target==OrderStatus.DELIVERED) return;
        login(buyer.getEmail(),"USER");
        if(status(id)==OrderStatus.DELIVERED) history.requestReturn(buyer.getEmail(),id);
        if(target==OrderStatus.RETURN_REQUESTED) return;
        login(shop.getName().equals("StarShop Võ Văn Ngân") ? "vendor.vovanngan@starshop.local" : "vendor.nguyengiati@starshop.local","VENDOR");
        if(status(id)==OrderStatus.RETURN_REQUESTED) vendor.markReturned(id,shop.getId(),shop.getOwner().getId());
        if(target==OrderStatus.RETURNED) return;
        if(status(id)==OrderStatus.RETURNED) vendor.refundOrder(id,shop.getId(),shop.getOwner().getId());
    }
    private void login(String email,String role) {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(email,"",AuthorityUtils.createAuthorityList("ROLE_"+role)));
    }
}
