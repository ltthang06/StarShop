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
    private String returnUrl="http://127.0.0.1:8080/payments/vnpay/return";

    public boolean ready() {return enabled && !tmnCode.isBlank() && !hashSecret.isBlank();}
    public void requireReady() {
        if(!ready()) throw new IllegalArgumentException("Thanh toán VNPAY chưa được cấu hình");
        if(!paymentUrl.equals("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"))
            throw new IllegalArgumentException("Kết nối này chỉ dành cho VNPAY Sandbox");
        var url=java.net.URI.create(returnUrl);
        if(url.getHost()==null || url.getRawUserInfo()!=null || url.getFragment()!=null || returnUrl.length()>255
                || !("http".equals(url.getScheme()) || "https".equals(url.getScheme())))
            throw new IllegalArgumentException("Địa chỉ nhận kết quả thanh toán không hợp lệ");
    }
}
