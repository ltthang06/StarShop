package vn.iotstar.starshop.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.SecurityUserRepository;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean
    public UserDetailsService userDetailsService(SecurityUserRepository users) {
        return email -> {
            var account = users.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));
            return User.withUsername(account.getEmail()).password(account.getPassword())
                    .roles(account.getRoles().stream().map(role -> role.getName().name()).toArray(String[]::new))
                    .disabled(account.getStatus() == UserStatus.INACTIVE)
                    .accountLocked(account.getStatus() == UserStatus.LOCKED).build();
        };
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers(paths("/", "/error", "/favicon.ico", "/css/**", "/js/**", "/images/**",
                        "/static/**", "/uploads/**", "/webjars/**", "/shop/**",
                        "/register", "/register/**", "/verify-otp", "/forgot-password", "/reset-password")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/products/**", "GET"),
                        new AntPathRequestMatcher("/categories/**", "GET")).permitAll()
                .requestMatchers(paths("/manager", "/manager/**")).hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(paths("/shipper", "/shipper/**")).hasAnyRole("SHIPPER", "ADMIN")
                .requestMatchers(paths("/vendor", "/vendor/**")).hasAnyRole("USER", "VENDOR", "ADMIN")
                .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults()).logout(Customizer.withDefaults());
        return http.build();
    }

    private static RequestMatcher[] paths(String... patterns) {
        return Arrays.stream(patterns).map(AntPathRequestMatcher::new).toArray(RequestMatcher[]::new);
    }
}
