package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import vn.iotstar.starshop.config.DemoCatalogImporter;
import vn.iotstar.starshop.repository.*;

@SpringBootTest(properties = {
    "spring.profiles.active=demo",
    "starshop.demo-catalog.enabled=true",
    "spring.datasource.url=${starshop.test.database-url:jdbc:h2:mem:catalog-import;MODE=MSSQLServer;DB_CLOSE_DELAY=-1}",
    "BOOTSTRAP_ADMIN_EMAIL=catalog-admin@example.test",
    "BOOTSTRAP_ADMIN_PASSWORD=CatalogTestPassword!",
    "starshop.demo-catalog.owner-email=catalog-admin@example.test"
})
class DemoCatalogImportTests {
    @Autowired DemoCatalogImporter importer;
    @Autowired ProductRepository products;
    @Autowired ProductImageRepository images;
    @Autowired com.fasterxml.jackson.databind.ObjectMapper mapper;

    @Test void importsFiftyDistinctProductsWithCloudinaryImagesAndLocalCopiesWithoutDuplicates() throws Exception {
        assertEquals(50,products.count());
        assertEquals(50,images.count());
        var hashes=new HashSet<String>();
        DemoCatalogImporter.Catalog catalog;
        try(var input=new ClassPathResource("demo/vuonhoatuoi-catalog.json").getInputStream()) {
            catalog=mapper.readValue(input,DemoCatalogImporter.Catalog.class);
        }
        for(var image:images.findAll()) {
            var item=catalog.products().stream().filter(p -> p.imagePublicId().equals(image.getPublicId())).findFirst().orElseThrow();
            assertEquals(item.imageUrl(),image.getImageUrl());
            assertTrue(image.getImageUrl().startsWith("https://res.cloudinary.com/"));
            byte[] bytes;
            try(var stream=new ClassPathResource("static/images/catalog/vuonhoatuoi/"+item.key().toLowerCase(java.util.Locale.ROOT)+".webp").getInputStream()) {
                bytes=stream.readAllBytes();
            }
            assertTrue(bytes.length>2000);
            assertEquals("RIFF",new String(bytes,0,4,java.nio.charset.StandardCharsets.US_ASCII));
            assertEquals("WEBP",new String(bytes,8,4,java.nio.charset.StandardCharsets.US_ASCII));
            hashes.add(java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)));
        }
        assertEquals(50,hashes.size());
        for(var product:products.findAll()) {
            assertFalse(product.getDescription().contains("vuonhoatuoi.vn"));
            assertFalse(product.getDescription().contains("demo"));
            assertTrue(product.getPrice().signum()>0);
            if(product.getDiscountPrice()!=null) assertTrue(product.getDiscountPrice().compareTo(product.getPrice())<0);
        }
        assertEquals(0,importer.importCatalog("catalog-admin@example.test"));
        assertEquals(50,products.count());
        assertEquals(50,images.count());
    }
}
