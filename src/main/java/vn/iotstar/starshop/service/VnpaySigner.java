package vn.iotstar.starshop.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.iotstar.starshop.config.VnpayProperties;

@Component @RequiredArgsConstructor
public class VnpaySigner {
    private final VnpayProperties config;
    public String query(Map<String,String> fields) {
        return new TreeMap<>(fields).entrySet().stream().filter(e -> e.getValue()!=null && !e.getValue().isEmpty())
                .map(e -> encode(e.getKey())+"="+encode(e.getValue())).collect(java.util.stream.Collectors.joining("&"));
    }
    private String encode(String value) {return URLEncoder.encode(value,StandardCharsets.UTF_8);}
    public String signature(Map<String,String> fields) {
        try {
            Mac mac=Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(config.getHashSecret().getBytes(StandardCharsets.UTF_8),"HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(query(fields).getBytes(StandardCharsets.UTF_8)));
        } catch(java.security.GeneralSecurityException ex) {throw new IllegalStateException("Không thể xác thực thanh toán",ex);}
    }
    public Map<String,String> verified(Map<String,String[]> parameters) {
        config.requireReady();
        if(parameters.size()>40) throw new IllegalArgumentException("Dữ liệu thanh toán không hợp lệ");
        Map<String,String> fields=new TreeMap<>();String hash=null;
        for(var entry:parameters.entrySet()) {
            if(!entry.getKey().startsWith("vnp_")) continue;
            if(entry.getValue()==null || entry.getValue().length!=1 || entry.getValue()[0].length()>1024)
                throw new IllegalArgumentException("Dữ liệu thanh toán bị lặp hoặc quá dài");
            if(entry.getKey().equals("vnp_SecureHash")) hash=entry.getValue()[0];
            else if(!entry.getKey().equals("vnp_SecureHashType")) fields.put(entry.getKey(),entry.getValue()[0]);
        }
        if(hash==null || !hash.matches("[0-9a-fA-F]{128}")
                || !MessageDigest.isEqual(signature(fields).getBytes(StandardCharsets.US_ASCII),hash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII))
                || !config.getTmnCode().equals(fields.get("vnp_TmnCode")))
            throw new IllegalArgumentException("Chữ ký thanh toán không hợp lệ");
        return fields;
    }
}
