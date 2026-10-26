package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.*;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.CustomerCatalogService;

@SpringBootTest(properties="spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional
class CatalogQueryTests {
    @Autowired EntityManager em;
    @Autowired CustomerCatalogService catalog;
    @Autowired UserRepository users;
    @Autowired ShopRepository shops;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired ProductImageRepository images;
    @Autowired vn.iotstar.starshop.service.CustomerOrderService orderService;

    @Test void orderHistoryLoadsLinesAndReviewFlagsInBatches() {
        var f=fixture();
        var product=product(f,"History flower",100000);
        Long reviewedId=null;
        for(int i=0;i<12;i++) {
            var order=new vn.iotstar.starshop.entity.Order();order.setUser(f.user());order.setShop(f.shop());
            order.setReceiverName("Buyer");order.setReceiverPhone("0900000000");order.setShippingAddress("Test address");
            order.setPaymentMethod(PaymentMethod.COD);order.setStatus(OrderStatus.DELIVERED);
            order.setSubtotal(BigDecimal.valueOf(200000));order.setTotalAmount(BigDecimal.valueOf(200000));
            em.persist(order);
            for(int j=0;j<2;j++) {
                var line=new OrderDetail();line.setOrder(order);line.setProduct(product);line.setQuantity(1);
                line.setUnitPrice(product.getPrice());line.setSubtotal(product.getPrice());em.persist(line);
                if(i==11 && j==0) {
                    var review=new Review();review.setUser(f.user());review.setProduct(product);review.setOrderDetail(line);
                    review.setRating(4);review.setContent("A review already submitted for this purchased flower product.");em.persist(review);
                    reviewedId=line.getId();
                }
            }
        }
        em.flush();em.clear();
        var stats=em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();stats.clear();
        var history=orderService.history(f.user().getEmail(),0);
        assertEquals(12,history.getTotalElements());assertEquals(10,history.getNumberOfElements());
        assertEquals(20,history.stream().mapToInt(o -> o.getLines().size()).sum());
        assertEquals(19,history.stream().flatMap(o -> o.getLines().stream()).filter(l -> l.isCanReview()).count());
        Long expectedReviewedId=reviewedId;
        assertTrue(history.stream().flatMap(o -> o.getLines().stream()).anyMatch(l -> l.getDetailId().equals(expectedReviewedId) && !l.isCanReview()));
        assertEquals(5,stats.getPrepareStatementCount());
    }

    @Test void pageUsesOneImageQueryAndSortsByDiscountedPrice() {
        var fixture=fixture();
        var expected=new ArrayList<Long>();
        for(int i=0;i<15;i++) {
            var p=product(fixture, "Query flower "+i, 1000000+i*10000);
            p.setDiscountPrice(BigDecimal.valueOf(300000-i*1000)); products.save(p);
            image(p,"/secondary-"+i,false);
            image(p,"/primary-"+i,true);
            expected.add(p.getId());
        }
        Collections.reverse(expected);
        em.flush();em.clear();
        var stats=em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();stats.clear();
        var page=catalog.search("Query flower",fixture.category().getId(),"price-asc",0);
        assertEquals(15,page.getTotalElements());
        assertEquals(expected.subList(0,12),page.getContent().stream().map(c -> c.getId()).toList());
        assertEquals("/primary-14",page.getContent().get(0).getImageUrl());

        assertEquals(3,stats.getPrepareStatementCount());
        Collections.reverse(expected);
        assertEquals(expected.subList(0,12),catalog.search("Query flower",fixture.category().getId(),"price-desc",0)
                .getContent().stream().map(c -> c.getId()).toList());
    }

    @Test void favoriteRankingUsesBatchFetchWithoutChangingTiesOrMissingImageFallback() {
        var fixture=fixture();
        var first=product(fixture,"Favorite first",100000);
        var second=product(fixture,"Favorite second",200000);
        var third=product(fixture,"Favorite third",300000);
        image(first,"/fallback-lowest-id",false);image(first,"/later",false);
        favorite(fixture.user(),first);favorite(fixture.user(),second);favorite(fixture.user(),third);
        var other=new User();other.setEmail(UUID.randomUUID()+"@example.test");other.setFullName("Second buyer");other.setPassword("unused");other=users.save(other);
        favorite(other,first);
        em.flush();em.clear();
        var stats=em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();stats.clear();
        var cards=catalog.top20("favorites");
        assertEquals(List.of(first.getId(),third.getId(),second.getId()),cards.stream().map(c -> c.getId()).toList());
        assertEquals("/fallback-lowest-id",cards.get(0).getImageUrl());
        assertNull(cards.get(1).getImageUrl());
        assertEquals(3,stats.getPrepareStatementCount());
    }

    private record Fixture(User user,Shop shop,Category category) {}
    private Fixture fixture() {
        var user=new User();user.setEmail(UUID.randomUUID()+"@example.test");user.setFullName("Query buyer");user.setPassword("unused");user=users.save(user);
        var shop=new Shop();shop.setOwner(user);shop.setName("Query shop");shop.setStatus(ShopStatus.ACTIVE);shop=shops.save(shop);
        var category=new Category();category.setName("Query "+UUID.randomUUID());category=categories.save(category);
        return new Fixture(user,shop,category);
    }
    private Product product(Fixture f,String name,int price) {
        var p=new Product();p.setName(name);p.setDescription("Catalog query test");p.setPrice(BigDecimal.valueOf(price));p.setShop(f.shop());p.setCategory(f.category());p.setQuantity(5);return products.save(p);
    }
    private void image(Product p,String url,boolean primary) {
        var image=new ProductImage();image.setProduct(p);image.setImageUrl(url);image.setPrimaryImage(primary);images.save(image);
    }
    private void favorite(User user,Product product) {
        var w=new Wishlist();w.setUser(user);w.setProduct(product);em.persist(w);
    }
}
