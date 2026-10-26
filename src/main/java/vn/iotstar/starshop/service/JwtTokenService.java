package vn.iotstar.starshop.service;
import java.time.Instant;
import java.util.HexFormat;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
@Service @RequiredArgsConstructor
public class JwtTokenService {
    private final AuthenticationManager apiAuthenticationManager;
    private final JwtEncoder encoder;
    private final org.springframework.security.core.userdetails.UserDetailsService users;
    public Map<String,Object> issue(String email,String password) {
        var auth=apiAuthenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email,password));
        UserDetails user=users.loadUserByUsername(auth.getName());
        if (!user.isEnabled() || !user.isAccountNonLocked()
                || (auth.getPrincipal() instanceof vn.iotstar.starshop.config.StarShopPrincipal principal
                    && !principal.matchesCredential(user.getPassword())))
            throw new BadCredentialsException("Tài khoản đã thay đổi. Vui lòng đăng nhập lại");
        Instant now=Instant.now();
        var claims=JwtClaimsSet.builder().issuer("starshop").subject(user.getUsername()).issuedAt(now)
            .expiresAt(now.plusSeconds(900)).claim("credential",fingerprint(user.getPassword())).build();
        String token=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        return Map.of("access_token",token,"token_type","Bearer","expires_in",900);
    }
    public static String fingerprint(String password) {
        try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8)));}
        catch (java.security.NoSuchAlgorithmException ex) {throw new IllegalStateException(ex);}
    }
}
