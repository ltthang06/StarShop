package vn.iotstar.starshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.DispatcherType;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(
                                DispatcherType.FORWARD,
                                DispatcherType.ERROR
                        )
                        .permitAll()

                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/shop/**"),
                                new AntPathRequestMatcher("/products/**"),
                                new AntPathRequestMatcher("/error"),
                                new AntPathRequestMatcher("/favicon.ico"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/images/**"),
                                new AntPathRequestMatcher("/uploads/**"),
                                new AntPathRequestMatcher("/webjars/**")
                        )
                        .permitAll()

                        .requestMatchers(
                                new AntPathRequestMatcher("/vendor/**"),
                                new AntPathRequestMatcher("/shipper/**")
                        )
                        .authenticated()

                        .anyRequest()
                        .authenticated()
                )

                .formLogin(form -> form
                        .permitAll()
                )

                .logout(logout -> logout
                        .permitAll()
                );

        return http.build();
    }
}