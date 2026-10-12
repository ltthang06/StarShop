package vn.iotstar.starshop.config;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
public class JwtConfig {
    @Bean public SecretKeySpec jwtKey(@Value("${JWT_SECRET:}") String configured) {
        byte[] bytes;
        if (configured.isBlank()) {bytes=new byte[32];new SecureRandom().nextBytes(bytes);}
        else {
            bytes=configured.getBytes(StandardCharsets.UTF_8);
            if(bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET phải có ít nhất 32 byte");
        }
        return new SecretKeySpec(bytes,"HmacSHA256");
    }
    @Bean public JwtEncoder jwtEncoder(SecretKeySpec key) {return new NimbusJwtEncoder(new ImmutableSecret<>(key));}
    @Bean public JwtDecoder jwtDecoder(SecretKeySpec key) {
        NimbusJwtDecoder decoder=NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("starshop"));return decoder;
    }
    @Bean public AuthenticationManager apiAuthenticationManager(UserDetailsService users,PasswordEncoder encoder) {
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
    @Bean @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http,UserDetailsService users) throws Exception {
        http.securityMatcher(new AntPathRequestMatcher("/api/**"))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/api/auth/token","POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/manager/**")).hasAnyRole("MANAGER","ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(resource -> resource.jwt(jwt -> jwt.jwtAuthenticationConverter(token -> {
                var account=users.loadUserByUsername(token.getSubject());
                if(!account.isEnabled() || !account.isAccountNonLocked()
                    || !vn.iotstar.starshop.service.JwtTokenService.fingerprint(account.getPassword()).equals(token.getClaimAsString("credential")))
                    throw new BadCredentialsException("Tài khoản hoặc token không còn hợp lệ");
                // Quyền được đọc lại từ DB để khóa tài khoản/thay đổi quyền có hiệu lực ngay với API.
                return new JwtAuthenticationToken(token,account.getAuthorities(),account.getUsername());
            })));
        return http.build();
    }
}
