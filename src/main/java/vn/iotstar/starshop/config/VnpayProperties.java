package vn.iotstar.starshop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component @ConfigurationProperties(prefix="starshop.vnpay") @Getter @Setter
public class VnpayProperties {
    private boolean enabled;
    private boolean reconciliationEnabled;
    private String tmnCode="";
    private String hashSecret="";
    private String paymentUrl="https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl="";

    public boolean ready() {return enabled && !tmnCode.isBlank() && !hashSecret.isBlank();}
    public void requireReady() {
        if(!ready()) throw new IllegalArgumentException("Thanh toán VNPAY chưa được cấu hình");
        if(!paymentUrl.equals("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"))
            throw new IllegalArgumentException("Kết nối này chỉ dành cho VNPAY Sandbox");
        if(!returnUrl.isBlank()) validateReturnUrl(returnUrl);
    }
    public String resolveReturnUrl(String browserReturnUrl) {
        String resolved=returnUrl.isBlank() ? browserReturnUrl : returnUrl;
        validateReturnUrl(resolved);
        return resolved;
    }
    private void validateReturnUrl(String value) {
        if(value==null || value.isBlank()) throw new IllegalArgumentException("Chưa xác định được địa chỉ nhận kết quả thanh toán");
        var url=java.net.URI.create(value);
        if(url.getHost()==null || url.getRawUserInfo()!=null || url.getFragment()!=null || value.length()>255
                || !("http".equals(url.getScheme()) || "https".equals(url.getScheme())))
            throw new IllegalArgumentException("Địa chỉ nhận kết quả thanh toán không hợp lệ");
    }
}
