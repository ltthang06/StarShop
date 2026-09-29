package vn.iotstar.starshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.SecurityUserRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(SecurityUserRepository users) {
        return email -> {
            vn.iotstar.starshop.entity.User account = users.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));
            String[] roles = account.getRoles().stream()
                    .map(role -> role.getName().name())
                    .toArray(String[]::new);
            return User.withUsername(account.getEmail())
                    .password(account.getPassword())
                    .roles(roles)
                    .disabled(account.getStatus() == UserStatus.INACTIVE)
                    .accountLocked(account.getStatus() == UserStatus.LOCKED)
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/css/**", "/js/**", "/images/**", "/static/**", "/shop/**",
                        "/register", "/register/**", "/verify-otp", "/forgot-password", "/reset-password")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/products/**", "/categories/**").permitAll()
                .requestMatchers("/manager", "/manager/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/shipper", "/shipper/**").hasAnyRole("SHIPPER", "ADMIN")
                .requestMatchers("/vendor", "/vendor/**").hasAnyRole("USER", "VENDOR", "ADMIN")
                .anyRequest().authenticated())
            .formLogin(Customizer.withDefaults())
            .logout(Customizer.withDefaults());
        return http.build();
    }
}
