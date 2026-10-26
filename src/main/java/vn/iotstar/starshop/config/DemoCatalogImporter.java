package vn.iotstar.starshop.config;

import java.math.BigDecimal;
import java.io.IOException;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;


@Component
@Profile("demo")
@ConditionalOnProperty(name="starshop.demo-catalog.enabled", havingValue="true")
@org.springframework.core.annotation.Order(0)
@RequiredArgsConstructor
public class DemoCatalogImporter implements ApplicationRunner {
    private final ObjectMapper mapper;
    private final SecurityUserRepository users;
    private final ShopRepository shops;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final ProductImageRepository images;
    @Value("${starshop.demo-catalog.owner-email:admin@starshop.local}") private String ownerEmail;

    public record Catalog(String source, String sourceUrl, String capturedAt, String usage, List<Item> products) {}
    public record Item(String key, String name, String category, BigDecimal price, BigDecimal discountPrice,
            String sourceUrl, String sourceImageUrl, String imageUrl, String imagePublicId, String description) {}

    @Override @Transactional
    public void run(ApplicationArguments arguments) throws IOException {
        importCatalog(ownerEmail);
    }

    @Transactional
    public int importCatalog(String email) throws IOException {
        User owner=users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Cần tạo chủ shop demo trước khi nạp danh mục"));
        Catalog catalog;
        try (var input=new ClassPathResource("demo/vuonhoatuoi-catalog.json").getInputStream()) {
            catalog=mapper.readValue(input,Catalog.class);
        }
        if (catalog.products()==null || catalog.products().size()!=50)
            throw new IllegalStateException("Danh mục hoa phải có đúng 50 mẫu");
        for (Item item:catalog.products()) {
            if (item.price()==null || item.price().signum()<=0 || item.name()==null || item.name().isBlank()
                || item.imageUrl()==null || item.imagePublicId()==null
                || !item.imagePublicId().matches("starshop/products/[A-Za-z0-9_-]+")
                || !item.imageUrl().matches("https://res\\.cloudinary\\.com/[A-Za-z0-9_-]+/image/upload/v[0-9]+/"
                    +java.util.regex.Pattern.quote(item.imagePublicId())+"\\.webp")
                || !new ClassPathResource("static/images/catalog/vuonhoatuoi/"+item.key().toLowerCase(Locale.ROOT)+".webp").exists())
                throw new IllegalStateException("Dữ liệu hoặc ảnh không hợp lệ: "+item.key());
        }
        String shopName="StarShop Central";
        Shop shop=shops.findAll().stream().filter(s -> s.getOwner().getId().equals(owner.getId())
                && shopName.equals(s.getName())).findFirst().orElse(null);
        if (shop==null) {
            shop=new Shop();shop.setOwner(owner);shop.setName(shopName);shop.setStatus(ShopStatus.ACTIVE);
            shop.setDescription("Bộ sưu tập hoa dành cho sinh nhật, chúc mừng và những dịp đặc biệt.");
            shop=shops.save(shop);
        }
        Map<String,Category> categoryMap=new HashMap<>();
        categories.findAll().forEach(c -> categoryMap.put(c.getName(),c));
        final Long shopId=shop.getId();
        Set<String> existing=new HashSet<>();
        Set<String> importedKeys=new HashSet<>();
        images.findAll().forEach(image -> importedKeys.add(image.getPublicId()));
        products.findAll((root,query,cb) -> cb.equal(root.get("shop").get("id"),shopId))
                .forEach(p -> existing.add(p.getName()));
        int imported=0;
        for (Item item:catalog.products()) {
            if (importedKeys.contains("catalog-vuonhoatuoi-"+item.key()) || importedKeys.contains(item.imagePublicId())) continue;
            if (existing.contains(item.name())) continue;
            Category category=categoryMap.get(item.category());
            if(category==null) {
                category=new Category();category.setName(item.category());category.setActive(true);
                category=categories.save(category);categoryMap.put(item.category(),category);
            }
            Product product=new Product();
            product.setName(item.name());product.setDescription(item.description());
            product.setPrice(item.price());product.setDiscountPrice(item.discountPrice());
            product.setShop(shop);product.setCategory(category);product.setQuantity(30);

            product.setSoldCount(12+imported);product.setRating(0);product.setStatus(ProductStatus.ACTIVE);
            product=products.save(product);
            ProductImage image=new ProductImage();image.setProduct(product);image.setImageUrl(item.imageUrl());
            image.setPublicId(item.imagePublicId());image.setPrimaryImage(true);images.save(image);
            existing.add(item.name());imported++;
        }
        return imported;
    }
}
