package vn.iotstar.starshop.service;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.iotstar.starshop.config.VnpayProperties;

@Component
@RequiredArgsConstructor
public class VnpayQueryGateway {
    private final VnpayProperties config;
    private final ObjectMapper mapper;
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final List<String> RESPONSE_FIELDS=List.of("vnp_ResponseId","vnp_Command","vnp_ResponseCode","vnp_Message",
            "vnp_TmnCode","vnp_TxnRef","vnp_Amount","vnp_BankCode","vnp_PayDate","vnp_TransactionNo",
            "vnp_TransactionType","vnp_TransactionStatus","vnp_OrderInfo","vnp_PromotionCode","vnp_PromotionAmount");

    public static final class VerifiedResult {
        private final Map<String,String> fields;
        private VerifiedResult(Map<String,String> fields) {this.fields=Map.copyOf(fields);}
        public Map<String,String> fields() {return fields;}
    }

    public VerifiedResult query(VnpayPaymentService.QueryTarget target) {
        config.requireReady();
        var time=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        Map<String,String> fields=new LinkedHashMap<>();
        fields.put("vnp_RequestId",UUID.randomUUID().toString().replace("-",""));
        fields.put("vnp_Version","2.1.0");fields.put("vnp_Command","querydr");fields.put("vnp_TmnCode",target.merchantCode());
        fields.put("vnp_TxnRef",target.reference());fields.put("vnp_TransactionDate",target.createdAt().format(time));
        fields.put("vnp_CreateDate",LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(time));
        fields.put("vnp_IpAddr","127.0.0.1");fields.put("vnp_OrderInfo","Truy van thanh toan StarShop "+target.reference());
        fields.put("vnp_SecureHash",checksum(String.join("|",fields.values())));
        try {
            var request=HttpRequest.newBuilder(URI.create("https://sandbox.vnpayment.vn/merchant_webapi/api/transaction"))
                    .timeout(Duration.ofSeconds(15)).header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(fields))).build();
            var response=client.send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()!=200) throw new IllegalArgumentException("VNPAY chưa phản hồi kết quả đối soát");
            Map<String,String> result=mapper.readValue(response.body(),new TypeReference<Map<String,String>>() {});
            return verify(result,target);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();throw new IllegalArgumentException("Kết nối đối soát bị gián đoạn");
        } catch(Exception ex) {
            throw new IllegalArgumentException("Chưa xác minh được kết quả từ VNPAY");
        }
    }

    public VerifiedResult verify(Map<String,String> fields,VnpayPaymentService.QueryTarget target) {
        String received=fields.get("vnp_SecureHash");
        String data=RESPONSE_FIELDS.stream().map(k -> Objects.toString(fields.get(k),"")).collect(java.util.stream.Collectors.joining("|"));
        if(received==null || !received.matches("[0-9a-fA-F]{128}")
                || !MessageDigest.isEqual(checksum(data).getBytes(StandardCharsets.US_ASCII),received.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII))
                || !"00".equals(fields.get("vnp_ResponseCode")) || !"querydr".equals(fields.get("vnp_Command"))
                || !config.getTmnCode().equals(target.merchantCode()) || !target.merchantCode().equals(fields.get("vnp_TmnCode"))
                || !target.reference().equals(fields.get("vnp_TxnRef")) || !"01".equals(fields.get("vnp_TransactionType"))
                || !Set.of("00","01","02","04","05","06","07","09").contains(Objects.toString(fields.get("vnp_TransactionStatus"),"")))
            throw new IllegalArgumentException("Không xác minh được phản hồi tra cứu VNPAY");
        String amount=fields.get("vnp_Amount");
        if(amount==null || !amount.matches("[0-9]{1,18}") || new java.math.BigDecimal(amount).compareTo(target.amount().movePointRight(2))!=0)
            throw new IllegalArgumentException("Số tiền đối soát không khớp");
        Map<String,String> clean=new HashMap<>();
        for(var entry:fields.entrySet()) if(entry.getValue()!=null) clean.put(entry.getKey(),entry.getValue());
        return new VerifiedResult(clean);
    }

    private String checksum(String data) {
        try {
            var mac=Mac.getInstance("HmacSHA512");mac.init(new SecretKeySpec(config.getHashSecret().getBytes(StandardCharsets.UTF_8),"HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch(java.security.GeneralSecurityException ex) {throw new IllegalStateException("Không thể ký yêu cầu đối soát",ex);}
    }
}
