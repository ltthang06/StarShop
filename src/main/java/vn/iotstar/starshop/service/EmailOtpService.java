package vn.iotstar.starshop.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public void send(String recipient, String code, String purpose) {
        if (mailHost == null || mailHost.isBlank()
                || fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình máy chủ gửi email");
        }

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("Chưa cấu hình dịch vụ gửi email");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setFrom(fromAddress);
        message.setSubject("StarShop - mã xác thực " + purpose);
        message.setText(
                "Mã xác thực của bạn là " + code
                        + ". Mã có hiệu lực trong 10 phút."
        );
        try {
            sender.send(message);
        } catch (MailException ex) {
            throw new IllegalStateException(
                    "Không gửi được mã xác thực qua email", ex);
        }
    }
}
