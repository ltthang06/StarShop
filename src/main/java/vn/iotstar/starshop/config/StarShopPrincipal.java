package vn.iotstar.starshop.config;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import vn.iotstar.starshop.service.JwtTokenService;


public final class StarShopPrincipal extends User {
    private static final long serialVersionUID = 1L;
    private final String credentialFingerprint;

    public StarShopPrincipal(String email, String encodedPassword, boolean enabled, boolean unlocked,
            Collection<? extends GrantedAuthority> authorities) {
        super(email, encodedPassword, enabled, true, true, unlocked, authorities);
        credentialFingerprint=JwtTokenService.fingerprint(encodedPassword);
    }

    public boolean matchesCredential(String encodedPassword) {
        return java.security.MessageDigest.isEqual(
                credentialFingerprint.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                JwtTokenService.fingerprint(encodedPassword).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
