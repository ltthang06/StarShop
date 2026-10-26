package vn.iotstar.starshop.service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.starshop.config.VnpayProperties;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.enums.*;
import vn.iotstar.starshop.repository.*;

@Service @RequiredArgsConstructor
public class VnpayPaymentService {
    private static final ZoneId ZONE=ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter TIME=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final VnpayProperties config;
    private final VnpaySigner signer;
    private final VnpayPaymentRepository payments;
    private final OrderRepository orders;
    private final OrderInventoryService inventory;
    private final NotificationService notifications;

    @Transactional
    public String create(String email,List<Long> ids,String clientIp) {
        config.requireReady();
        var payment=new VnpayPayment();payment.setReference("SS"+UUID.randomUUID().toString().replace("-",""));
        var sorted=ids.stream().distinct().sorted().toList();
        if(sorted.isEmpty() || sorted.size()!=ids.size()) throw new IllegalArgumentException("Danh sách đơn hàng không hợp lệ");
        BigDecimal total=BigDecimal.ZERO;
        for(Long id:sorted) {
            var order=orders.findForUpdate(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
            if(!order.getUser().getEmail().equalsIgnoreCase(email) || order.getPaymentMethod()!=PaymentMethod.VNPAY
                    || order.getPaymentStatus()!=PaymentStatus.PENDING || order.getStatus()!=OrderStatus.NEW)
                throw new IllegalArgumentException("Đơn hàng không thể thanh toán VNPAY");
            payment.setUser(order.getUser());payment.getOrders().add(order);total=total.add(order.getTotalAmount());
        }
        if(total.signum()<=0) throw new IllegalArgumentException("Số tiền VNPAY phải lớn hơn 0");
        total.movePointRight(2).longValueExact();
        payment.setAmount(total);payment.setCreatedAt(LocalDateTime.now(ZONE));payment.setExpiresAt(payment.getCreatedAt().plusMinutes(15));
        payment.setMerchantCode(config.getTmnCode());
        payment.setClientIp(clientIp!=null && clientIp.matches("[0-9a-fA-F:.]{3,45}") && !clientIp.equals("::1") ? clientIp : "127.0.0.1");
        payment.setGatewayTransactionNo("pending-"+payment.getReference());payments.save(payment);return payment.getReference();
    }

    @Transactional(readOnly=true)
    public Map<String,Object> view(String reference,String email) {
        var p=owned(reference,email);
        var view=new HashMap<String,Object>();
        view.put("reference",p.getReference());view.put("amount",p.getAmount());view.put("paymentState",p.getState().name());
        view.put("expiresAt",p.getExpiresAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        view.put("orderIds",p.getOrders().stream().map(o -> o.getId()).sorted().toList());
        boolean expired=!p.getExpiresAt().isAfter(LocalDateTime.now(ZONE));
        view.put("expired",expired);view.put("returned",p.getReturnReceivedAt()!=null);
        view.put("reportedSuccess","00".equals(p.getReturnResponseCode()) && "00".equals(p.getReturnTransactionStatus()));
        view.put("canPay",config.ready() && p.getState()==VnpayPayment.State.PENDING && !expired && p.getReturnReceivedAt()==null
                && Objects.equals(p.getMerchantCode(),config.getTmnCode()));
        view.put("canCheck",config.ready() && p.getState()==VnpayPayment.State.PENDING);
        return view;
    }

    @Transactional(readOnly=true)
    public String referenceForOrder(Long id,String email) {
        return payments.findOwnedByOrder(id,email).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch của bạn")).getReference();
    }
    @Transactional
    public String redirectUrl(String reference,String email) {
        config.requireReady();owned(reference,email);
        var p=payments.findForUpdate(reference).orElseThrow();
        if(p.getState()!=VnpayPayment.State.PENDING || p.getReturnReceivedAt()!=null
                || !Objects.equals(p.getMerchantCode(),config.getTmnCode())
                || !p.getExpiresAt().isAfter(LocalDateTime.now(ZONE)))
            throw new IllegalArgumentException("Giao dịch đã kết thúc hoặc hết hạn thanh toán");
        if(p.getGatewayStartedAt()==null) p.setGatewayStartedAt(LocalDateTime.now(ZONE));
        Map<String,String> fields=new TreeMap<>();
        fields.put("vnp_Version","2.1.0");fields.put("vnp_Command","pay");fields.put("vnp_TmnCode",config.getTmnCode());
        fields.put("vnp_Amount",p.getAmount().movePointRight(2).toBigIntegerExact().toString());fields.put("vnp_CurrCode","VND");
        fields.put("vnp_TxnRef",p.getReference());fields.put("vnp_OrderInfo","Thanh toan hoa StarShop "+p.getReference());
        fields.put("vnp_OrderType","other");fields.put("vnp_Locale","vn");fields.put("vnp_ReturnUrl",config.getReturnUrl());
        fields.put("vnp_IpAddr",p.getClientIp());fields.put("vnp_CreateDate",p.getCreatedAt().format(TIME));fields.put("vnp_ExpireDate",p.getExpiresAt().format(TIME));
        return config.getPaymentUrl()+"?"+signer.query(fields)+"&vnp_SecureHash="+signer.signature(fields);
    }
    private VnpayPayment owned(String ref,String email) {
        if(ref==null || !ref.matches("SS[0-9a-f]{32}") || email==null)
            throw new IllegalArgumentException("Không tìm thấy giao dịch của bạn");
        return payments.findById(ref).filter(p -> p.getUser().getEmail().equalsIgnoreCase(email))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch của bạn"));
    }

    @Transactional
    public Map<String,String> ipn(Map<String,String[]> parameters) {
        Map<String,String> fields;
        try {fields=signer.verified(parameters);} catch(IllegalArgumentException ex) {return reply("97","Invalid signature");}
        return applyResult(fields);
    }

    private Map<String,String> applyResult(Map<String,String> fields) {
        String ref=fields.get("vnp_TxnRef");
        if(ref==null || !ref.matches("SS[0-9a-f]{32}")) return reply("01","Order not found");
        var p=payments.findForUpdate(ref).orElse(null);
        if(p==null) return reply("01","Order not found");
        if(p.getMerchantCode()!=null && !p.getMerchantCode().equals(fields.get("vnp_TmnCode"))) return reply("97","Merchant mismatch");
        if(!validAmount(p,fields)) return reply("04","Invalid amount");
        String response=fields.get("vnp_ResponseCode"),state=fields.get("vnp_TransactionStatus");
        if(response==null || state==null || !response.matches("[0-9]{2}") || !state.matches("[0-9]{2}")) return reply("99","Invalid result");
        if(p.getState()!=VnpayPayment.State.PENDING) {
            if(p.getState()==VnpayPayment.State.FAILED && response.equals("00") && state.equals("00")) {
                p.setState(VnpayPayment.State.REVIEW_REQUIRED);p.setResponseCode(response);p.setTransactionStatus(state);
            }
            return reply("02","Order already confirmed");
        }
        if(state.equals("01")) return reply("99","Transaction processing");
        boolean success=response.equals("00") && state.equals("00");
        String transactionNo=fields.get("vnp_TransactionNo");
        if(success && (transactionNo==null || !transactionNo.matches("[0-9]{1,15}") || transactionNo.matches("0+")))
            return reply("99","Invalid transaction number");
        var locked=new ArrayList<vn.iotstar.starshop.entity.Order>();
        for(Long id:p.getOrders().stream().map(o -> o.getId()).sorted().toList()) locked.add(orders.findForUpdate(id).orElseThrow());
        p.setReconciliationNote(null);
        p.setResponseCode(response);p.setTransactionStatus(state);p.setResolvedAt(LocalDateTime.now(ZONE));
        if(success) {
            p.setGatewayTransactionNo(transactionNo);
            if(locked.stream().anyMatch(o -> o.getStatus()!=OrderStatus.NEW || o.getPaymentStatus()!=PaymentStatus.PENDING)) {
                p.setState(VnpayPayment.State.REVIEW_REQUIRED);return reply("00","Recorded for reconciliation");
            }
            p.setState(VnpayPayment.State.PAID);
            locked.forEach(o -> {o.setPaymentStatus(PaymentStatus.PAID);notifications.orderChanged(o);});
        } else if(response.equals("07") || Set.of("04","05","06","07","09").contains(state)) {
            p.setState(VnpayPayment.State.REVIEW_REQUIRED);
        } else {
            p.setState(VnpayPayment.State.FAILED);
            inventory.restoreMany(locked.stream().filter(o -> o.getStatus()==OrderStatus.NEW && o.getPaymentStatus()==PaymentStatus.PENDING).map(o -> o.getId()).toList());
            for(var o:locked) {
                if(o.getStatus()==OrderStatus.NEW && o.getPaymentStatus()==PaymentStatus.PENDING) {
                    o.setStatus(OrderStatus.CANCELLED);o.setPaymentStatus(PaymentStatus.FAILED);notifications.orderChanged(o);
                }
            }
        }
        payments.flush();return reply("00","Confirm success");
    }
    @Transactional
    public void handleReturn(Map<String,String[]> parameters,String email) {
        var fields=signer.verified(parameters);
        owned(fields.get("vnp_TxnRef"),email);
        recordReturn(fields,email);
        if("00".equals(fields.get("vnp_ResponseCode")) && "00".equals(fields.get("vnp_TransactionStatus"))) {
            String code=applyResult(fields).get("RspCode");
            if(!"00".equals(code) && !"02".equals(code))
                throw new IllegalArgumentException("Chưa xác nhận được kết quả thanh toán");
        }
    }

    @Transactional
    public void recordReturn(Map<String,String> fields,String email) {
        var owner=owned(fields.get("vnp_TxnRef"),email);
        var p=payments.findForUpdate(owner.getReference()).orElseThrow();
        if(!config.getTmnCode().equals(fields.get("vnp_TmnCode")) || !validAmount(p,fields)
                || (p.getMerchantCode()!=null && !p.getMerchantCode().equals(fields.get("vnp_TmnCode"))))
            throw new IllegalArgumentException("Kết quả thanh toán không khớp giao dịch");
        String response=fields.get("vnp_ResponseCode"),state=fields.get("vnp_TransactionStatus");
        if(response==null || state==null || !response.matches("[0-9]{2}") || !state.matches("[0-9]{2}"))
            throw new IllegalArgumentException("Kết quả thanh toán không hợp lệ");
        if(p.getMerchantCode()==null) p.setMerchantCode(fields.get("vnp_TmnCode"));
        if(p.getGatewayStartedAt()==null) p.setGatewayStartedAt(p.getCreatedAt());
        if(p.getReturnReceivedAt()==null) p.setReturnReceivedAt(LocalDateTime.now(ZONE));
        p.setReturnResponseCode(response);p.setReturnTransactionStatus(state);
    }

    public record QueryTarget(String reference,String merchantCode,LocalDateTime createdAt,BigDecimal amount) {}

    @Transactional
    public QueryTarget prepareReconciliation(String reference,String email) {
        if(email!=null) owned(reference,email);
        var p=payments.findForUpdate(reference).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch"));
        if(p.getState()!=VnpayPayment.State.PENDING) return null;
        LocalDateTime now=LocalDateTime.now(ZONE);
        if(p.getLastCheckedAt()!=null && p.getLastCheckedAt().isAfter(now.minusSeconds(60))) return null;
        p.setLastCheckedAt(now);
        if(!Objects.equals(p.getMerchantCode(),config.getTmnCode())) {
            p.setReconciliationNote("Giao dịch dùng cấu hình Merchant cũ hoặc chưa xác định. Cần đối chiếu với VNPAY trước khi xử lý.");
            return null;
        }
        if(p.getGatewayStartedAt()==null && p.getReturnReceivedAt()==null && p.getExpiresAt().isBefore(now.minusMinutes(2))) {
            applyResult(Map.of("vnp_TxnRef",reference,"vnp_TmnCode",p.getMerchantCode(),
                    "vnp_Amount",p.getAmount().movePointRight(2).toBigIntegerExact().toString(),
                    "vnp_ResponseCode","24","vnp_TransactionStatus","02","vnp_TransactionNo","0"));
            p.setReconciliationNote("Hủy đơn quá hạn chưa từng mở cổng thanh toán; đã hoàn tồn kho.");
            return null;
        }
        if(p.getGatewayStartedAt()==null) return null;
        return new QueryTarget(p.getReference(),p.getMerchantCode(),p.getCreatedAt(),p.getAmount());
    }

    @Transactional
    public void applyQueryResult(VnpayQueryGateway.VerifiedResult result) {
        var fields=result.fields();
        if(!config.getTmnCode().equals(fields.get("vnp_TmnCode"))) throw new IllegalArgumentException("Merchant không khớp");
        var p=payments.findForUpdate(fields.get("vnp_TxnRef")).orElseThrow();
        if(p.getState()!=VnpayPayment.State.PENDING) return;
        if(!validAmount(p,fields)) throw new IllegalArgumentException("Số tiền không khớp");
        if("01".equals(fields.get("vnp_TransactionStatus"))) {
            p.setReconciliationNote("VNPAY chưa hoàn tất giao dịch. Chưa hủy đơn hoặc hoàn tồn kho.");return;
        }
        String code=applyResult(fields).get("RspCode");
        if(!"00".equals(code) && !"02".equals(code)) throw new IllegalArgumentException("Chưa xác nhận được kết quả giao dịch");
    }

    @Transactional
    public void reconciliationUnavailable(String reference) {
        payments.findForUpdate(reference).filter(p -> p.getState()==VnpayPayment.State.PENDING).ifPresent(p ->
                p.setReconciliationNote("Chưa xác minh được kết quả từ VNPAY. Cửa hàng cần đối chiếu, chưa hủy đơn hoặc hoàn tồn kho."));
    }

    @Transactional(readOnly=true)
    public org.springframework.data.domain.Page<Map<String,Object>> unresolved(int page) {
        return payments.findUnresolved(List.of(VnpayPayment.State.PENDING,VnpayPayment.State.REVIEW_REQUIRED),org.springframework.data.domain.PageRequest.of(Math.max(0,page),20)).map(ref -> {
            var p=payments.findById(ref).orElseThrow();var row=new HashMap<String,Object>();
            row.put("reference",ref);row.put("amount",p.getAmount());row.put("state",p.getState().name());
            row.put("orders",p.getOrders().stream().map(o -> o.getId()).sorted().toList());
            row.put("expiresAt",p.getExpiresAt());row.put("lastCheckedAt",p.getLastCheckedAt());
            row.put("note",p.getReconciliationNote());row.put("legacy",!Objects.equals(p.getMerchantCode(),config.getTmnCode()));
            return (Map<String,Object>)row;
        });
    }

    public boolean validAmount(VnpayPayment p,Map<String,String> fields) {
        String amount=fields.get("vnp_Amount");
        return amount!=null && amount.matches("[0-9]{1,18}") && new BigDecimal(amount).compareTo(p.getAmount().movePointRight(2))==0;
    }
    private Map<String,String> reply(String code,String message) {return Map.of("RspCode",code,"Message",message);}
}
