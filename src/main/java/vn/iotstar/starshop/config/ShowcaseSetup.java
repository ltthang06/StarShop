package vn.iotstar.starshop.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;

@Component @Profile("demo") @ConditionalOnProperty(name="starshop.showcase.enabled",havingValue="true")
@org.springframework.core.annotation.Order(10) @RequiredArgsConstructor
public class ShowcaseSetup implements ApplicationRunner {
    private final SecurityUserRepository users;
    private final RoleRepository roles;
    private final ShopRepository shops;
    private final ProductRepository products;
    private final ShippingProviderRepository providers;
    private final CustomerPromotionRepository promotions;
    private final CustomerAddressRepository addresses;
    private final CommissionRepository commissions;
    private final PasswordEncoder encoder;
    @Value("${starshop.showcase.password}") private String password;

    @Override @Transactional
    public void run(ApplicationArguments args) {
        if(password.length()<12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("Mật khẩu tài khoản vận hành cần 12–72 byte");
        var manager1=account("manager.thuduc@starshop.local","Nguyễn Minh Anh",RoleName.MANAGER);
        var manager2=account("manager.binhthanh@starshop.local","Trần Ngọc Linh",RoleName.MANAGER);
        var vendor1=account("vendor.thuduc@starshop.local","Lê Thanh Hà",RoleName.VENDOR);
        var vendor2=account("vendor.binhthanh@starshop.local","Phạm Hoàng Nam",RoleName.VENDOR);
        account("shipper@starshop.local","Võ Thành Đạt",RoleName.SHIPPER);
        var buyer=account("customer@starshop.local","Nguyễn Khánh An",RoleName.USER);
        var central=shops.findAll().stream().filter(s -> "StarShop Central".equals(s.getName())).findFirst().orElseThrow();
        central.setAddress("Quận 1, TP. Hồ Chí Minh");central.setPhone("02838220001");central.setManager(manager1);
        var thuduc=shop("StarShop Thủ Đức",vendor1,manager1,"TP. Thủ Đức, TP. Hồ Chí Minh","02838220002");
        var binhthanh=shop("StarShop Bình Thạnh",vendor2,manager2,"Bình Thạnh, TP. Hồ Chí Minh","02838220003");
        var catalog=products.findAll().stream().sorted(Comparator.comparing(Product::getId)).toList();
        if(catalog.size()!=50) throw new IllegalStateException("Cần danh mục đúng 50 sản phẩm trước khi thiết lập cửa hàng");
        for(int i=0;i<catalog.size();i++) {
            var p=catalog.get(i);
            if(p.getShop().getId().equals(central.getId())) p.setShop(i<17 ? central : i<34 ? thuduc : binhthanh);
        }
        provider("StarShop Express",new BigDecimal("25000"),1);
        provider("StarShop Standard",new BigDecimal("18000"),2);
        promotion("HOATUOI10","Ưu đãi hoa tươi 10%",PromotionType.PERCENT,new BigDecimal("10"),null);
        promotion("FREESHIP","Ưu đãi giao hoa",PromotionType.FREE_SHIPPING,BigDecimal.ZERO,null);
        promotion("THUDUC50","Thủ Đức giảm 50.000đ",PromotionType.FIXED_AMOUNT,new BigDecimal("50000"),thuduc);
        address(buyer,"Nhà riêng","0901234001","TP. Thủ Đức",true);
        address(buyer,"Văn phòng","0901234002","Bình Thạnh",false);
        for(var s:List.of(central,thuduc,binhthanh)) {
            if(commissions.findFirstByShopIdAndActiveTrueOrderByStartAtDesc(s.getId()).isEmpty()) {
                var c=new Commission();c.setShop(s);c.setRatePercent(new BigDecimal("5"));c.setStartAt(LocalDateTime.now());c.setActive(true);commissions.save(c);
            }
        }
    }

    private User account(String email,String name,RoleName role) {
        var existing=users.findByEmailIgnoreCase(email);
        if(existing.isPresent()) {
            if(existing.get().getRoles().stream().noneMatch(r -> r.getName()==role))
                throw new IllegalStateException("Tài khoản đã có nhưng khác vai trò: "+email);
            return existing.get();
        }
        var user=new User();user.setEmail(email);user.setFullName(name);user.setPassword(encoder.encode(password));user.setStatus(UserStatus.ACTIVE);
        for(var required:role==RoleName.VENDOR ? List.of(RoleName.USER,role) : List.of(role)) {
            var assigned=roles.findByName(required).orElseGet(() -> {var r=new Role();r.setName(required);return roles.save(r);});
            user.getRoles().add(assigned);
        }
        return users.save(user);
    }
    private Shop shop(String name,User owner,User manager,String address,String phone) {
        var existing=shops.findAll().stream().filter(s -> name.equals(s.getName()) && owner.getId().equals(s.getOwner().getId())).findFirst();
        if(existing.isPresent()) return existing.get();
        var s=new Shop();s.setName(name);s.setOwner(owner);s.setManager(manager);s.setStatus(ShopStatus.ACTIVE);s.setAddress(address);s.setPhone(phone);
        s.setDescription("Hoa tươi cho sinh nhật, chúc mừng và những dịp đặc biệt. Giao hoa tận nơi tại TP. Hồ Chí Minh.");return shops.save(s);
    }
    private void provider(String name,BigDecimal fee,int days) {
        if(providers.findAll().stream().anyMatch(p -> name.equals(p.getName()))) return;
        var p=new ShippingProvider();p.setName(name);p.setBaseFee(fee);p.setEstimatedDays(days);p.setActive(true);providers.save(p);
    }
    private void promotion(String code,String name,PromotionType type,BigDecimal value,Shop shop) {
        if(promotions.findByCodeIgnoreCase(code).isPresent()) return;
        var p=new Promotion();p.setCode(code);p.setName(name);p.setType(type);p.setDiscountValue(value);p.setMinOrderAmount(new BigDecimal("200000"));
        p.setMaxDiscountAmount(new BigDecimal("100000"));p.setQuantity(100);p.setStartAt(LocalDateTime.now().minusDays(1));p.setEndAt(LocalDateTime.now().plusMonths(3));
        p.setScope(shop==null ? PromotionScope.SYSTEM : PromotionScope.SHOP);p.setShop(shop);p.setActive(true);promotions.save(p);
    }
    private void address(User user,String label,String phone,String district,boolean primary) {
        String line=label.equals("Nhà riêng") ? "12 Đường Hoa" : "25 Đường Số 3";
        if(addresses.findByUserIdOrderByDefaultAddressDescIdAsc(user.getId()).stream().anyMatch(a -> line.equals(a.getAddressLine()))) return;
        var a=new Address();a.setUser(user);a.setRecipientName(user.getFullName());a.setPhone(phone);a.setAddressLine(line);a.setDistrict(district);a.setProvince("TP. Hồ Chí Minh");a.setDefaultAddress(primary);addresses.save(a);
    }
}
