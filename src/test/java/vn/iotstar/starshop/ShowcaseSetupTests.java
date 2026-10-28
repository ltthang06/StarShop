package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import vn.iotstar.starshop.config.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;

@SpringBootTest(properties={"spring.profiles.active=demo","starshop.demo-catalog.enabled=true",
        "starshop.showcase.enabled=true","starshop.showcase.orders-enabled=true","starshop.showcase.password=ShowcaseTestPassword!",
        "BOOTSTRAP_ADMIN_EMAIL=admin@starshop.local","BOOTSTRAP_ADMIN_PASSWORD=ShowcaseAdminPassword!",
        "spring.datasource.url=${starshop.test.database-url:jdbc:h2:mem:showcase;MODE=MSSQLServer;DB_CLOSE_DELAY=-1}"})
class ShowcaseSetupTests {
    @Autowired ShowcaseSetup setup;
    @Autowired ShowcaseOrderSetup workflow;
    @Autowired DemoCatalogImporter importer;
    @Autowired ProductRepository products;
    @Autowired ShopRepository shops;
    @Autowired OrderRepository orders;
    @Autowired SecurityUserRepository users;
    @Autowired ShippingProviderRepository providers;
    @Autowired CustomerPromotionRepository promotions;
    @Autowired CustomerReviewRepository reviews;
    @Autowired ShipperAssignmentRepository assignments;

    @Test void fillsAllRolesAndOrderStagesThroughServicesAndCanRunAgain() throws Exception {
        assertEquals(50,products.count());assertEquals(3,shops.count());assertEquals(7,users.count());
        assertEquals(2,providers.count());assertEquals(3,promotions.count());assertEquals(13,orders.count());assertEquals(1,reviews.count());
        var expected=EnumSet.of(OrderStatus.NEW,OrderStatus.CONFIRMED,OrderStatus.READY_FOR_PICKUP,OrderStatus.ASSIGNED,OrderStatus.PICKED_UP,
                OrderStatus.SHIPPING,OrderStatus.DELIVERED,OrderStatus.CANCELLED,OrderStatus.RETURN_REQUESTED,OrderStatus.RETURNED,OrderStatus.REFUNDED);
        assertEquals(expected,orders.findAll().stream().map(o -> o.getStatus()).collect(java.util.stream.Collectors.toSet()));
        assertEquals(9,assignments.count());
        for(var p:products.findAll()) assertTrue(p.getQuantity()>=0);
        assertEquals(0,importer.importCatalog("admin@starshop.local"));
        setup.run(new DefaultApplicationArguments());workflow.run(new DefaultApplicationArguments());
        assertEquals(50,products.count());assertEquals(3,shops.count());assertEquals(7,users.count());assertEquals(13,orders.count());assertEquals(1,reviews.count());
    }
}
