package vn.iotstar.starshop.config;

import java.util.Arrays;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            SecurityUserRepository userRepository) {

        return email -> {
            var account = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Không tìm thấy tài khoản"));

            String[] roles = account.getRoles().stream()
                    .map(role -> role.getName().name())
                    .toArray(String[]::new);

            return new StarShopPrincipal(account.getEmail(), account.getPassword(),
                    account.getStatus() == UserStatus.ACTIVE,
                    account.getStatus() != UserStatus.LOCKED,
                    org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(
                            Arrays.stream(roles).map(role -> "ROLE_"+role).toArray(String[]::new)));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityUserRepository users)
            throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(
                        DispatcherType.FORWARD,
                        DispatcherType.ERROR
                ).permitAll()
                .requestMatchers(paths(
                        "/",
                        "/login",
                        "/register",
                        "/register/resend",
                        "/verify-otp",
                        "/forgot-password",
                        "/reset-password",
                        "/payments/vnpay/ipn",
                        "/flower-chat",
                        "/shop/**",
                        "/error",
                        "/favicon.ico",
                        "/css/**",
                        "/assets/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**",
                        "/webjars/**"
                )).permitAll()
                .requestMatchers(
                        new AntPathRequestMatcher("/products", "GET"),
                        new AntPathRequestMatcher("/products/**", "GET")
                ).permitAll()
                .requestMatchers(paths("/manager/payments", "/manager/payments/**")).hasRole("ADMIN")
                .requestMatchers(paths("/manager/users", "/manager/users/**",
                        "/manager/categories", "/manager/categories/**",
                        "/manager/promotions", "/manager/promotions/**",
                        "/manager/shipping-providers", "/manager/shipping-providers/**")).hasRole("ADMIN")
                .requestMatchers(paths("/manager/shops/*/manager", "/manager/shops/*/commission",
                        "/manager/shops/*/approve", "/manager/shops/*/reject",
                        "/manager/shops/*/block", "/manager/shops/*/reopen")).hasRole("ADMIN")
                .requestMatchers(paths("/manager", "/manager/**"))
                .hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(paths("/admin/**"))
                .hasRole("ADMIN")
                .requestMatchers(paths("/shipper/**"))
                .hasAnyRole("SHIPPER", "ADMIN")
                .requestMatchers(paths("/vendor/**"))
                .hasAnyRole("USER", "VENDOR", "ADMIN")
                .anyRequest()
                .authenticated()
        ).formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/", false)
                .permitAll()
        ).logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
        );

        http.addFilterAfter(new ActiveAccountFilter(users),
                org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class);
        return http.build();
    }

    private static RequestMatcher[] paths(String... patterns) {
        return Arrays.stream(patterns)
                .map(AntPathRequestMatcher::new)
                .toArray(RequestMatcher[]::new);
    }
}
