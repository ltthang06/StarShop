package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.config.ViewFormat;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.CustomerCatalogService;

@SpringBootTest
class RankingPaginationTests {
    @Autowired CustomerCatalogService catalog;
    @Autowired ManagerUserRepository users;
    @Autowired ShopRepository shops;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired ViewFormat format;

    @Test @Transactional
    void paginationKeepsExactlyTop20AndPreservesRankOrder() {
        User owner=new User();owner.setEmail(UUID.randomUUID()+"@example.test");owner.setFullName("Rank owner");
        owner.setPassword("unused");owner.setStatus(UserStatus.ACTIVE);owner=users.save(owner);
        Shop shop=new Shop();shop.setOwner(owner);shop.setName("Rank shop");shop.setStatus(ShopStatus.ACTIVE);shop=shops.save(shop);
        Category category=new Category();category.setName("Rank "+UUID.randomUUID());category=categories.save(category);
        List<Long> expected=new ArrayList<>();
        for(int i=0;i<30;i++) {
            Product p=new Product();p.setName("Rank product "+i);p.setDescription("Ranking test");
            p.setPrice(new BigDecimal("100000"));p.setQuantity(1);p.setCategory(category);p.setShop(shop);
            p.setCreatedAt(LocalDateTime.of(2100,1,1,0,0).plusMinutes(i));
            expected.add(products.save(p).getId());
        }
        Collections.reverse(expected);
        List<Long> actual=new ArrayList<>();
        for(int page=0;page<3;page++) {
            var ranked=catalog.top20("newest",page);
            assertEquals(20,ranked.getTotalElements());
            assertEquals(3,ranked.getTotalPages());
            assertEquals(page==2 ? 4 : 8,ranked.getNumberOfElements());
            ranked.forEach(card -> actual.add(card.getId()));
        }
        assertEquals(expected.subList(0,20),actual);
        assertEquals(0,catalog.top20("newest",-1).getNumber());
        assertEquals(2,catalog.top20("newest",Integer.MAX_VALUE).getNumber());
        assertEquals("375.000 ₫",format.money(new BigDecimal("375000.00")));
        assertEquals("Chờ lấy hàng",format.label(OrderStatus.READY_FOR_PICKUP));
    }
}

