package vn.iotstar.starshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/"),
                    new AntPathRequestMatcher("/products/**"),
                    new AntPathRequestMatcher("/login"),
                    new AntPathRequestMatcher("/register"),
                    new AntPathRequestMatcher("/static/**"),
                    new AntPathRequestMatcher("/css/**"),
                    new AntPathRequestMatcher("/js/**"),
                    new AntPathRequestMatcher("/images/**")
                ).permitAll()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form.disable())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }
}