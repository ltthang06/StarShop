package vn.iotstar.starshop.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.entity.OtpToken;
import vn.iotstar.starshop.entity.Role;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.RoleName;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.OtpTokenRepository;
import vn.iotstar.starshop.repository.RoleRepository;
import vn.iotstar.starshop.repository.SecurityUserRepository;

@Service
@RequiredArgsConstructor
public class CustomerAuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecurityUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpTokenRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailOtpService emailOtpService;

    @Transactional
    public String register(
            String fullName,
            String email,
            String password,
            String confirmPassword) {

        String normalizedEmail = normalizeEmail(email);
        validatePassword(password, confirmPassword);

        if (fullName == null || fullName.isBlank()
                || fullName.trim().length() > 100) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên");
        }
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.USER);
                    return roleRepository.save(role);
                });

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(UserStatus.INACTIVE);
        user.getRoles().add(userRole);
        userRepository.save(user);

        sendNewCode(user, "kích hoạt tài khoản");
        return normalizedEmail;
    }

    @Transactional
    public void resendActivation(String email) {
        User user = findUser(email);
        if (user.getStatus() != UserStatus.INACTIVE) {
            throw new IllegalArgumentException("Tài khoản không chờ kích hoạt");
        }
        sendNewCode(user, "kích hoạt tài khoản");
    }

    @Transactional
    public void activate(String email, String code) {
        User user = findUser(email);
        if (user.getStatus() != UserStatus.INACTIVE) {
            throw new IllegalArgumentException("Tài khoản không chờ kích hoạt");
        }
        useCode(user, code);
        user.setStatus(UserStatus.ACTIVE);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .ifPresent(user -> sendNewCode(user, "đặt lại mật khẩu"));
    }

    @Transactional
    public void resetPassword(
            String email,
            String code,
            String password,
            String confirmPassword) {

        validatePassword(password, confirmPassword);
        User user = findUser(email);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Tài khoản chưa hoạt động");
        }

        useCode(user, code);
        user.setPassword(passwordEncoder.encode(password));
    }

    private void sendNewCode(User user, String purpose) {
        otpRepository.findByUserIdAndUsedFalse(user.getId())
                .forEach(token -> token.setUsed(true));

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        OtpToken token = new OtpToken();
        token.setUser(user);
        token.setCode(code);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(token);

        emailOtpService.send(user.getEmail(), code, purpose);
    }

    private void useCode(User user, String code) {
        OtpToken token = otpRepository
                .findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(
                        user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mã xác thực không hợp lệ"));

        if (code == null
                || !token.getCode().equals(code.trim())
                || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Mã xác thực không hợp lệ");
        }
        token.setUsed(true);
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tài khoản"));
    }

    private String normalizeEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePassword(String password, String confirmPassword) {
        if (password == null || password.length() < 8
                || password.length() > 72) {
            throw new IllegalArgumentException(
                    "Mật khẩu phải dài từ 8 đến 72 ký tự");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "Mật khẩu xác nhận không khớp");
        }
    }
}
