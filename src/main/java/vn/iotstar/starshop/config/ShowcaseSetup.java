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
    private final ProductImageRepository images;
    private final CategoryRepository categories;
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
        renameAccount("vendor.thuduc@starshop.local","vendor.vovanngan@starshop.local");
        renameAccount("vendor.binhthanh@starshop.local","vendor.nguyengiati@starshop.local");
        renameAccount("manager.thuduc@starshop.local","manager1@starshop.local");
        renameAccount("manager.binhthanh@starshop.local","manager2@starshop.local");
        renameShop("StarShop Thủ Đức","StarShop Võ Văn Ngân");
        renameShop("StarShop Bình Thạnh","StarShop Nguyễn Gia Trí");
        renameShop("StarShop Thủ Đức - Lê Văn Việt","StarShop Lê Văn Việt");
        renameShop("StarShop Bình Thạnh - Xô Viết Nghệ Tĩnh","StarShop Xô Viết Nghệ Tĩnh");
        var manager1=account("manager1@starshop.local","Nguyễn Minh Anh - Manager 1",RoleName.MANAGER);
        var manager2=account("manager2@starshop.local","Trần Ngọc Linh - Manager 2",RoleName.MANAGER);
        var vendor1=account("vendor.vovanngan@starshop.local","Lê Thanh Hà",RoleName.VENDOR);
        var vendor2=account("vendor.nguyengiati@starshop.local","Phạm Hoàng Nam",RoleName.VENDOR);
        var vendor3=account("vendor.leuvanviet@starshop.local","Nguyễn Hoài Phương",RoleName.VENDOR);
        var vendor4=account("vendor.xovietnghetinh@starshop.local","Trần Quốc Bảo",RoleName.VENDOR);
        account("shipper@starshop.local","Võ Thành Đạt",RoleName.SHIPPER);
        var buyer=account("customer@starshop.local","Nguyễn Khánh An",RoleName.USER);
        var central=shops.findAll().stream().filter(s -> "StarShop Central".equals(s.getName())).findFirst().orElseThrow();
        central.setAddress("Quận 1, TP. Hồ Chí Minh");central.setPhone("02838220001");central.setManager(null);
        var thuduc=shop("StarShop Võ Văn Ngân",vendor1,manager1,"123 Võ Văn Ngân, TP. Thủ Đức, TP. Hồ Chí Minh","02838220002");
        var binhthanh=shop("StarShop Nguyễn Gia Trí",vendor2,manager2,"55 Nguyễn Gia Trí, Bình Thạnh, TP. Hồ Chí Minh","02838220003");
        var thuducLeVanViet=shop("StarShop Lê Văn Việt",vendor3,manager1,"198 Lê Văn Việt, TP. Thủ Đức, TP. Hồ Chí Minh","02838220004");
        var binhthanhXoViet=shop("StarShop Xô Viết Nghệ Tĩnh",vendor4,manager2,"225 Xô Viết Nghệ Tĩnh, Bình Thạnh, TP. Hồ Chí Minh","02838220005");
        var branchProducts=Set.of("Bó Hoa Thủ Đức Dịu Dàng","Kệ Hoa Bình Thạnh Tươi Sáng");
        var catalog=products.findAll().stream().filter(p -> !branchProducts.contains(p.getName())).sorted(Comparator.comparing(Product::getId)).toList();
        if(catalog.size()!=50) throw new IllegalStateException("Cần danh mục đúng 50 sản phẩm trước khi thiết lập cửa hàng");
        for(int i=0;i<catalog.size();i++) {
            var p=catalog.get(i);
            if(p.getShop().getId().equals(central.getId())) p.setShop(i<17 ? central : i<34 ? thuduc : binhthanh);
        }
        branchProduct(thuducLeVanViet,"Bó Hoa Thủ Đức Dịu Dàng","Bó hoa",new BigDecimal("520000"),14,"branch-levanviet");
        branchProduct(binhthanhXoViet,"Kệ Hoa Bình Thạnh Tươi Sáng","Kệ hoa",new BigDecimal("780000"),16,"branch-xovietnghetinh");
        provider("StarShop Express",new BigDecimal("25000"),1);
        provider("StarShop Standard",new BigDecimal("18000"),2);
        promotion("HOATUOI10","Ưu đãi hoa tươi 10%",PromotionType.PERCENT,new BigDecimal("10"),null);
        promotion("FREESHIP","Ưu đãi giao hoa",PromotionType.FREE_SHIPPING,BigDecimal.ZERO,null);
        promotion("THUDUC50","Thủ Đức giảm 50.000đ",PromotionType.FIXED_AMOUNT,new BigDecimal("50000"),thuduc);
        address(buyer,"Nhà riêng","0901234001","TP. Thủ Đức",true);
        address(buyer,"Văn phòng","0901234002","Bình Thạnh",false);
        for(var s:List.of(central,thuduc,binhthanh,thuducLeVanViet,binhthanhXoViet)) {
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
            existing.get().setFullName(name);
            return users.save(existing.get());
        }
        var user=new User();user.setEmail(email);user.setFullName(name);user.setPassword(encoder.encode(password));user.setStatus(UserStatus.ACTIVE);
        for(var required:role==RoleName.VENDOR ? List.of(RoleName.USER,role) : List.of(role)) {
            var assigned=roles.findByName(required).orElseGet(() -> {var r=new Role();r.setName(required);return roles.save(r);});
            user.getRoles().add(assigned);
        }
        return users.save(user);
    }
    private void renameAccount(String oldEmail,String newEmail) {
        var oldUser=users.findByEmailIgnoreCase(oldEmail);
        if(oldUser.isEmpty()) return;
        if(users.findByEmailIgnoreCase(newEmail).isPresent())
            throw new IllegalStateException("Tồn tại đồng thời hai tài khoản vận hành: "+oldEmail);
        oldUser.get().setEmail(newEmail);
        users.save(oldUser.get());
    }
    private Shop shop(String name,User owner,User manager,String address,String phone) {
        var existing=shops.findAll().stream().filter(s -> name.equals(s.getName())).findFirst();
        if(existing.isPresent()) {
            var s=existing.get();s.setOwner(owner);s.setManager(manager);s.setAddress(address);s.setPhone(phone);s.setStatus(ShopStatus.ACTIVE);
            return shops.save(s);
        }
        var s=new Shop();s.setName(name);s.setOwner(owner);s.setManager(manager);s.setStatus(ShopStatus.ACTIVE);s.setAddress(address);s.setPhone(phone);
        s.setDescription("Hoa tươi cho sinh nhật, chúc mừng và những dịp đặc biệt. Giao hoa tận nơi tại TP. Hồ Chí Minh.");return shops.save(s);
    }
    private void renameShop(String oldName,String newName) {
        shops.findAll().stream().filter(s -> oldName.equals(s.getName())).findFirst().ifPresent(s -> s.setName(newName));
    }
    private void branchProduct(Shop shop,String name,String categoryName,BigDecimal price,int soldCount,String imageKey) {
        var category=categories.findByNameContainingIgnoreCaseOrderByNameAsc(categoryName).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Thiếu danh mục bó hoa"));
        var product=products.findAll().stream().filter(p -> name.equals(p.getName()) && shop.getId().equals(p.getShop().getId())).findFirst().orElse(null);
        if(product==null) {
            product=new Product();product.setName(name);product.setDescription("Hoa tươi được chuẩn bị tại chi nhánh "+shop.getName()+".");
            product.setPrice(price);product.setQuantity(20);product.setSoldCount(soldCount);product.setRating(0);
            product.setStatus(ProductStatus.ACTIVE);product.setShop(shop);product.setCategory(category);products.save(product);
        }
        if(images.countByProductId(product.getId())==0) {
            var image=new ProductImage();image.setProduct(product);image.setPrimaryImage(true);
            image.setImageUrl("https://res.cloudinary.com/ckaav9vf/image/upload/starshop/products/"+imageKey+".webp");
            image.setPublicId("starshop/products/"+imageKey);images.save(image);
        }
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
