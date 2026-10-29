package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import vn.iotstar.starshop.config.VnpayProperties;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;
import vn.iotstar.starshop.service.*;

@SpringBootTest(properties={"starshop.vnpay.enabled=true","starshop.vnpay.tmn-code=TESTCODE",
        "starshop.vnpay.hash-secret=TestOnlyVnpayKeyForIntegrationChecks","starshop.sitemesh.enabled=false"})
@AutoConfigureMockMvc
class VnpayPaymentTests {
    @Autowired VnpayProperties config;
    @Autowired VnpaySigner signer;
    @Autowired VnpayPaymentService payments;
    @Autowired OnlineCheckoutService checkout;
    @Autowired CustomerCartService cart;
    @Autowired CustomerOrderService history;
    @Autowired OrderService vendorOrders;
    @Autowired VnpayPaymentRepository paymentRows;
    @Autowired UserRepository users;
    @Autowired ShopRepository shops;
    @Autowired CategoryRepository categories;
    @Autowired ProductRepository products;
    @Autowired CustomerAddressRepository addresses;
    @Autowired ShippingProviderRepository providers;
    @Autowired OrderRepository orders;
    @Autowired MockMvc mvc;
    @Autowired VnpayReconciliationService reconciliation;
    @org.springframework.boot.test.mock.mockito.SpyBean VnpayQueryGateway queryGateway;

    @Test void paymentUsesPublicHttpsOriginForReturnInsteadOfLocalhost() throws Exception {
        var f=fixture();String ref=place(f);
        String previous=config.getReturnUrl();config.setReturnUrl("");
        try {
            var result=mvc.perform(get("/payments/"+ref+"/redirect")
                    .with(user(f.buyer().getEmail()).roles("USER"))
                    .with(request -> {request.setScheme("https");request.setServerName("starshop.example");request.setServerPort(443);return request;}))
                    .andExpect(status().is3xxRedirection()).andReturn();
            String url=java.net.URLDecoder.decode(result.getResponse().getHeader("Location"),java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(url.contains("vnp_ReturnUrl=https://starshop.example/payments/vnpay/return"));
            assertFalse(url.contains("127.0.0.1"+":8080/payments/vnpay/return"));
        } finally {config.setReturnUrl(previous);}
    }

    @Test void checkoutGroupsTwoShopsAndProcessesIpnOnce() throws Exception {
        var f=fixture();String ref=place(f);
        var p=paymentRows.findById(ref).orElseThrow();assertEquals(new BigDecimal("250000.00"),p.getAmount());
        var url=payments.redirectUrl(ref,f.buyer().getEmail());
        assertTrue(url.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertTrue(url.contains("vnp_Amount=25000000"));assertTrue(url.contains("vnp_TmnCode=TESTCODE"));
        assertFalse(url.contains(config.getHashSecret()));
        var callback=callback(ref,"00","00","100001");
        var returnRequest=get("/payments/vnpay/return").with(user(f.buyer().getEmail()).roles("USER"));
        callback(ref,"24","02","0").forEach(returnRequest::param);
        mvc.perform(returnRequest).andExpect(status().isOk()).andExpect(model().attribute("paymentState","PENDING"));
        assertEquals(VnpayPayment.State.PENDING,paymentRows.findById(ref).orElseThrow().getState());
        assertEquals(false,payments.view(ref,f.buyer().getEmail()).get("canPay"));
        assertEquals(false,payments.view(ref,f.buyer().getEmail()).get("reportedSuccess"));
        assertThrows(IllegalArgumentException.class,() -> payments.redirectUrl(ref,f.buyer().getEmail()));
        var ids=history.history(f.buyer().getEmail(),0).map(c -> c.getId()).getContent();assertEquals(2,ids.size());
        var first=orders.findById(ids.get(0)).orElseThrow();
        assertThrows(IllegalArgumentException.class,() -> vendorOrders.confirmOrder(first.getId(),first.getShop().getId(),f.vendor().getId()));
        assertThrows(IllegalArgumentException.class,() -> history.cancel(f.buyer().getEmail(),first.getId()));
        var ipn=get("/payments/vnpay/ipn");callback.forEach(ipn::param);
        mvc.perform(ipn).andExpect(status().isOk()).andExpect(jsonPath("$.RspCode").value("00"));
        assertEquals(VnpayPayment.State.PAID,paymentRows.findById(ref).orElseThrow().getState());
        for(Long id:ids) assertEquals(PaymentStatus.PAID,orders.findById(id).orElseThrow().getPaymentStatus());
        mvc.perform(get("/payments/"+ref+"/status").with(user(f.buyer().getEmail()).roles("USER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("PAID"));
        mvc.perform(get("/payments/"+ref+"/status").with(user("another@example.test").roles("USER")))
                .andExpect(status().isNotFound());
        assertEquals("02",payments.ipn(callback).get("RspCode"));
        for(var product:f.products()) assertEquals(4,products.findById(product.getId()).orElseThrow().getQuantity());
        vendorOrders.confirmOrder(first.getId(),first.getShop().getId(),f.vendor().getId());
    }

    @Test void callbackRejectsTamperingWrongTerminalAmountAndDuplicateParameters() {
        var f=fixture();String ref=place(f);
        var tampered=callback(ref,"00","00","100002");tampered.put("vnp_Amount",new String[]{"100"});
        assertEquals("97",payments.ipn(tampered).get("RspCode"));
        var wrongAmount=fields(ref,"00","00","100002");wrongAmount.put("vnp_Amount","100");
        assertEquals("04",payments.ipn(sign(wrongAmount)).get("RspCode"));
        var wrongTerminal=fields(ref,"00","00","100002");wrongTerminal.put("vnp_TmnCode","OTHER");
        assertEquals("97",payments.ipn(sign(wrongTerminal)).get("RspCode"));
        var duplicate=callback(ref,"00","00","100002");duplicate.put("vnp_TxnRef",new String[]{ref,ref});
        assertEquals("97",payments.ipn(duplicate).get("RspCode"));
        assertEquals("99",payments.ipn(callback(ref,"00","00","0")).get("RspCode"));
        assertEquals(VnpayPayment.State.PENDING,paymentRows.findById(ref).orElseThrow().getState());
        assertThrows(IllegalArgumentException.class,() -> payments.view(ref,"another@example.test"));
        assertThrows(IllegalArgumentException.class,() -> payments.redirectUrl(ref,"another@example.test"));
    }

    @Test void unsuccessfulPaymentCancelsAndRestoresInventoryExactlyOnce() {
        var f=fixture();String ref=place(f);
        var failure=callback(ref,"24","02","0");
        assertEquals("00",payments.ipn(failure).get("RspCode"));assertEquals("02",payments.ipn(failure).get("RspCode"));
        for(var card:history.history(f.buyer().getEmail(),0)) {
            assertEquals(OrderStatus.CANCELLED,card.getStatus());assertEquals(PaymentStatus.FAILED,card.getPaymentStatus());
        }
        for(var product:f.products()) {var saved=products.findById(product.getId()).orElseThrow();assertEquals(5,saved.getQuantity());assertEquals(0,saved.getSoldCount());}
        assertEquals("02",payments.ipn(callback(ref,"00","00","100003")).get("RspCode"));
        assertEquals(VnpayPayment.State.REVIEW_REQUIRED,paymentRows.findById(ref).orElseThrow().getState());
        for(var product:f.products()) assertEquals(5,products.findById(product.getId()).orElseThrow().getQuantity());
    }

    @Test void processingAndUncertainResultsDoNotPretendPaymentFailedOrRestoreStock() {
        var f=fixture();String ref=place(f);
        assertEquals("99",payments.ipn(callback(ref,"00","01","0")).get("RspCode"));
        assertEquals(VnpayPayment.State.PENDING,paymentRows.findById(ref).orElseThrow().getState());
        assertEquals("00",payments.ipn(callback(ref,"07","07","100004")).get("RspCode"));
        assertEquals(VnpayPayment.State.REVIEW_REQUIRED,paymentRows.findById(ref).orElseThrow().getState());
        for(var product:f.products()) assertEquals(4,products.findById(product.getId()).orElseThrow().getQuantity());
    }

    @Test void concurrentIpnOnlyFinalizesOnce() throws Exception {
        var f=fixture();String ref=place(f);var callback=callback(ref,"00","00","100005");
        var ready=new CountDownLatch(2);var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try {
            Callable<String> action=() -> {ready.countDown();if(!start.await(10,TimeUnit.SECONDS)) throw new IllegalStateException();return payments.ipn(callback).get("RspCode");};
            var first=pool.submit(action);var second=pool.submit(action);assertTrue(ready.await(10,TimeUnit.SECONDS));start.countDown();
            assertEquals(Set.of("00","02"),Set.of(first.get(20,TimeUnit.SECONDS),second.get(20,TimeUnit.SECONDS)));
        } finally {start.countDown();pool.shutdownNow();}
        assertEquals(VnpayPayment.State.PAID,paymentRows.findById(ref).orElseThrow().getState());
    }

    @Test void configurationFailureDoesNotConsumeCartOrCreateOrders() {
        var f=fixture();long before=orders.count();config.setEnabled(false);
        try {assertThrows(IllegalArgumentException.class,() -> place(f));assertEquals(before,orders.count());assertEquals(2,cart.summary(f.buyer().getEmail()).getLines().size());}
        finally {config.setEnabled(true);}
        assertEquals("a=hello+world&b=%C4%91",signer.query(Map.of("b","đ","a","hello world")));
    }

    @Test void expirationPreventsNewRedirectButDelayedVerifiedPaymentIsStillRecorded() {
        var f=fixture();String ref=place(f);var row=paymentRows.findById(ref).orElseThrow();row.setExpiresAt(java.time.LocalDateTime.now().minusDays(1));paymentRows.save(row);
        assertThrows(IllegalArgumentException.class,() -> payments.redirectUrl(ref,f.buyer().getEmail()));
        assertEquals("00",payments.ipn(callback(ref,"00","00","100006")).get("RspCode"));
        assertEquals(VnpayPayment.State.PAID,paymentRows.findById(ref).orElseThrow().getState());
    }

    @Test void oneGatewayTransactionCannotPayTwoDifferentPaymentGroups() {
        String first=place(fixture()),second=place(fixture());
        assertEquals("00",payments.ipn(callback(first,"00","00","100007")).get("RspCode"));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,() -> payments.ipn(callback(second,"00","00","100007")));
        assertEquals(VnpayPayment.State.PENDING,paymentRows.findById(second).orElseThrow().getState());
    }

    @Test void authenticatedReturnAutomaticallyConfirmsOnlyValidOwnedSuccessfulPayment() throws Exception {
        var f=fixture();String ref=place(f);var valid=callback(ref,"00","00","700005");
        assertThrows(IllegalArgumentException.class,() -> payments.handleReturn(valid,"another@example.test"));
        var tampered=new HashMap<>(valid);tampered.put("vnp_Amount",new String[]{"1"});
        assertThrows(IllegalArgumentException.class,() -> payments.handleReturn(tampered,f.buyer().getEmail()));
        assertEquals(VnpayPayment.State.PENDING,paymentRows.findById(ref).orElseThrow().getState());
        var request=get("/payments/vnpay/return").with(user(f.buyer().getEmail()).roles("USER"));valid.forEach(request::param);
        mvc.perform(request).andExpect(status().isOk()).andExpect(model().attribute("paymentState","PAID"));
        payments.handleReturn(valid,f.buyer().getEmail());
        assertEquals(VnpayPayment.State.PAID,paymentRows.findById(ref).orElseThrow().getState());
        assertEquals("02",payments.ipn(valid).get("RspCode"));
        for(var product:f.products()) assertEquals(4,products.findById(product.getId()).orElseThrow().getQuantity());
        org.mockito.Mockito.verify(queryGateway,org.mockito.Mockito.never()).query(org.mockito.ArgumentMatchers.any());
    }

    @Test void abandonedUnopenedPaymentExpiresAndRestoresOnlyOnce() {
        var f=fixture();String ref=place(f);expire(ref);
        reconciliation.reconcile(ref,f.buyer().getEmail());
        assertEquals(VnpayPayment.State.FAILED,paymentRows.findById(ref).orElseThrow().getState());
        reconciliation.reconcile(ref,f.buyer().getEmail());
        for(var product:f.products()) assertEquals(5,products.findById(product.getId()).orElseThrow().getQuantity());
        org.mockito.Mockito.verify(queryGateway,org.mockito.Mockito.never()).query(org.mockito.ArgumentMatchers.any());
    }

    @Test void verifiedServerQueryCanRecoverPaymentWithoutIpn() {
        var f=fixture();String ref=place(f);payments.redirectUrl(ref,f.buyer().getEmail());expire(ref);
        var verified=queryGateway.verify(queryFields(ref,"00","700001"),target(ref));
        org.mockito.Mockito.doReturn(verified).when(queryGateway).query(org.mockito.ArgumentMatchers.any());
        reconciliation.reconcile(ref,f.buyer().getEmail());
        assertEquals(VnpayPayment.State.PAID,paymentRows.findById(ref).orElseThrow().getState());
        for(var product:f.products()) assertEquals(4,products.findById(product.getId()).orElseThrow().getQuantity());
        assertEquals("02",payments.ipn(callback(ref,"00","00","700001")).get("RspCode"));
    }

    @Test void verifiedFailedServerQueryCancelsAndRestoresStockOnce() {
        var f=fixture();String ref=place(f);payments.redirectUrl(ref,f.buyer().getEmail());expire(ref);
        var verified=queryGateway.verify(queryFields(ref,"02","0"),target(ref));
        org.mockito.Mockito.doReturn(verified).when(queryGateway).query(org.mockito.ArgumentMatchers.any());
        reconciliation.reconcile(ref,f.buyer().getEmail());reconciliation.reconcile(ref,f.buyer().getEmail());
        assertEquals(VnpayPayment.State.FAILED,paymentRows.findById(ref).orElseThrow().getState());
        for(var product:f.products()) assertEquals(5,products.findById(product.getId()).orElseThrow().getQuantity());
    }

    @Test void unavailableQueryKeepsStockAndPreventsRepeatedCallsDuringCooldown() {
        var f=fixture();String ref=place(f);payments.redirectUrl(ref,f.buyer().getEmail());expire(ref);
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Provider unavailable")).when(queryGateway).query(org.mockito.ArgumentMatchers.any());
        reconciliation.reconcile(ref,f.buyer().getEmail());reconciliation.reconcile(ref,f.buyer().getEmail());
        var saved=paymentRows.findById(ref).orElseThrow();assertEquals(VnpayPayment.State.PENDING,saved.getState());
        assertNotNull(saved.getReconciliationNote());
        for(var product:f.products()) assertEquals(4,products.findById(product.getId()).orElseThrow().getQuantity());
        org.mockito.Mockito.verify(queryGateway,org.mockito.Mockito.times(1)).query(org.mockito.ArgumentMatchers.any());
    }

    @Test void legacyPaymentsAreNotCancelledUsingDifferentMerchantCredentials() {
        var f=fixture();String ref=place(f);expire(ref);var row=paymentRows.findById(ref).orElseThrow();
        row.setMerchantCode(null);paymentRows.save(row);
        reconciliation.reconcile(ref,f.buyer().getEmail());
        assertEquals(VnpayPayment.State.PENDING,paymentRows.findById(ref).orElseThrow().getState());
        for(var product:f.products()) assertEquals(4,products.findById(product.getId()).orElseThrow().getQuantity());
        org.mockito.Mockito.verify(queryGateway,org.mockito.Mockito.never()).query(org.mockito.ArgumentMatchers.any());
        assertThrows(IllegalArgumentException.class,() -> reconciliation.reconcile(ref,"another@example.test"));
    }

    @Test void processingQueryWaitsAndSignedLateIpnStillWorks() {
        var f=fixture();String ref=place(f);payments.redirectUrl(ref,f.buyer().getEmail());expire(ref);
        var verified=queryGateway.verify(queryFields(ref,"01","0"),target(ref));
        org.mockito.Mockito.doReturn(verified).when(queryGateway).query(org.mockito.ArgumentMatchers.any());
        reconciliation.reconcile(ref,f.buyer().getEmail());
        assertEquals(VnpayPayment.State.PENDING,paymentRows.findById(ref).orElseThrow().getState());
        assertEquals("00",payments.ipn(callback(ref,"00","00","700002")).get("RspCode"));
        assertEquals(VnpayPayment.State.PAID,paymentRows.findById(ref).orElseThrow().getState());
    }

    @Test void successfulIpnWinsOverAnOlderFailedQueryResponse() {
        var f=fixture();String ref=place(f);payments.redirectUrl(ref,f.buyer().getEmail());expire(ref);
        var failure=queryGateway.verify(queryFields(ref,"02","0"),target(ref));
        org.mockito.Mockito.doAnswer(invocation -> {
            assertEquals("00",payments.ipn(callback(ref,"00","00","700004")).get("RspCode"));
            return failure;
        }).when(queryGateway).query(org.mockito.ArgumentMatchers.any());
        reconciliation.reconcile(ref,f.buyer().getEmail());
        assertEquals(VnpayPayment.State.PAID,paymentRows.findById(ref).orElseThrow().getState());
        for(var product:f.products()) assertEquals(4,products.findById(product.getId()).orElseThrow().getQuantity());
    }

    @Test void queryVerificationRejectsInvalidSignatureAmountMerchantAndRefundTransactions() {
        String ref=place(fixture());var target=target(ref);var data=queryFields(ref,"00","700003");
        assertNotNull(queryGateway.verify(data,target));
        data.put("vnp_Amount","1");var tampered=data;assertThrows(IllegalArgumentException.class,() -> queryGateway.verify(tampered,target));
        data.put("vnp_SecureHash",queryHash(data));var wrongAmount=data;assertThrows(IllegalArgumentException.class,() -> queryGateway.verify(wrongAmount,target));
        data=queryFields(ref,"00","700003");data.put("vnp_TransactionType","02");data.put("vnp_SecureHash",queryHash(data));
        var refund=data;assertThrows(IllegalArgumentException.class,() -> queryGateway.verify(refund,target));
        data=queryFields(ref,"00","700003");data.put("vnp_TmnCode","OTHER");data.put("vnp_SecureHash",queryHash(data));
        var wrongMerchant=data;assertThrows(IllegalArgumentException.class,() -> queryGateway.verify(wrongMerchant,target));
    }

    @Test void reconciliationQueueIsAdminOnlyAndDoesNotCreateProviderCalls() throws Exception {
        mvc.perform(get("/manager/payments").with(user("manager@example.test").roles("MANAGER"))).andExpect(status().isForbidden());
        mvc.perform(get("/manager/payments").with(user("admin@example.test").roles("ADMIN"))).andExpect(status().isOk()).andExpect(view().name("manager/payments"));
        org.mockito.Mockito.verify(queryGateway,org.mockito.Mockito.never()).query(org.mockito.ArgumentMatchers.any());
    }

    private void expire(String ref) {
        var row=paymentRows.findById(ref).orElseThrow();row.setExpiresAt(java.time.LocalDateTime.now().minusMinutes(10));paymentRows.save(row);
    }
    private VnpayPaymentService.QueryTarget target(String ref) {
        var row=paymentRows.findById(ref).orElseThrow();return new VnpayPaymentService.QueryTarget(ref,row.getMerchantCode(),row.getCreatedAt(),row.getAmount());
    }
    private Map<String,String> queryFields(String ref,String state,String transaction) {
        var data=new HashMap<String,String>();data.put("vnp_ResponseId",UUID.randomUUID().toString());data.put("vnp_Command","querydr");
        data.put("vnp_ResponseCode","00");data.put("vnp_Message","Success");data.put("vnp_TmnCode","TESTCODE");data.put("vnp_TxnRef",ref);
        data.put("vnp_Amount","25000000");data.put("vnp_BankCode","NCB");data.put("vnp_PayDate","20261026150000");
        data.put("vnp_TransactionNo",transaction);data.put("vnp_TransactionType","01");data.put("vnp_TransactionStatus",state);
        data.put("vnp_OrderInfo","Test payment");data.put("vnp_PromotionCode","");data.put("vnp_PromotionAmount","");
        data.put("vnp_SecureHash",queryHash(data));return data;
    }
    private String queryHash(Map<String,String> fields) {
        try {
            var keys=List.of("vnp_ResponseId","vnp_Command","vnp_ResponseCode","vnp_Message","vnp_TmnCode","vnp_TxnRef",
                    "vnp_Amount","vnp_BankCode","vnp_PayDate","vnp_TransactionNo","vnp_TransactionType","vnp_TransactionStatus",
                    "vnp_OrderInfo","vnp_PromotionCode","vnp_PromotionAmount");
            String value=keys.stream().map(k -> fields.getOrDefault(k,"")).collect(java.util.stream.Collectors.joining("|"));
            var mac=javax.crypto.Mac.getInstance("HmacSHA512");
            mac.init(new javax.crypto.spec.SecretKeySpec(config.getHashSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8),"HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch(Exception ex) {throw new IllegalStateException(ex);}
    }

    private Map<String,String> fields(String ref,String response,String status,String transaction) {
        return new TreeMap<>(Map.of("vnp_TmnCode","TESTCODE","vnp_TxnRef",ref,"vnp_Amount","25000000",
                "vnp_ResponseCode",response,"vnp_TransactionStatus",status,"vnp_TransactionNo",transaction));
    }
    private Map<String,String[]> sign(Map<String,String> fields) {
        var result=new HashMap<String,String[]>();fields.forEach((k,v) -> result.put(k,new String[]{v}));result.put("vnp_SecureHash",new String[]{signer.signature(fields)});return result;
    }
    private Map<String,String[]> callback(String ref,String response,String status,String transaction) {return sign(fields(ref,response,status,transaction));}
    private String place(Fixture f) {return checkout.place(f.buyer().getEmail(),f.address().getId(),f.provider().getId(),"",null,"127.0.0.1");}
    private record Fixture(User buyer,User vendor,Address address,ShippingProvider provider,List<Product> products) {}
    private Fixture fixture() {
        var buyer=account();var vendor=account();var category=new Category();category.setName("Payment "+UUID.randomUUID());category=categories.save(category);
        var selection=new ArrayList<Product>();
        for(int i=0;i<2;i++) {
            var shop=new Shop();shop.setName("Payment shop "+i);shop.setOwner(vendor);shop.setStatus(ShopStatus.ACTIVE);shop=shops.save(shop);
            var p=new Product();p.setName("Payment flower");p.setDescription("Payment integration product");p.setShop(shop);p.setCategory(category);p.setPrice(new BigDecimal("100000"));p.setQuantity(5);p=products.save(p);selection.add(p);cart.add(buyer.getEmail(),p.getId(),1);
        }
        var address=new Address();address.setUser(buyer);address.setRecipientName("Buyer");address.setPhone("0900000000");address.setAddressLine("Test address");address=addresses.save(address);
        var provider=new ShippingProvider();provider.setName("Payment shipping "+UUID.randomUUID());provider.setBaseFee(new BigDecimal("25000"));provider=providers.save(provider);
        return new Fixture(buyer,vendor,address,provider,selection);
    }
    private User account() {var u=new User();u.setFullName("Payment buyer");u.setEmail(UUID.randomUUID()+"@example.test");u.setPassword("unused");u.setStatus(UserStatus.ACTIVE);return users.save(u);}
}
