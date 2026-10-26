package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import vn.iotstar.starshop.config.StarShopPrincipal;
import vn.iotstar.starshop.service.JwtTokenService;

class JwtIssuanceRaceTests {
    @Test void accountChangeBetweenAuthenticationAndTokenIssuanceCannotGrantFreshToken() {
        var manager=mock(AuthenticationManager.class);var users=mock(UserDetailsService.class);var encoder=mock(JwtEncoder.class);
        var original=new StarShopPrincipal("buyer@example.test","old-encoded-password",true,true,List.of());original.eraseCredentials();
        when(manager.authenticate(any())).thenReturn(UsernamePasswordAuthenticationToken.authenticated(original,null,List.of()));
        var service=new JwtTokenService(manager,encoder,users);
        for(var changed:List.of(
                User.withUsername(original.getUsername()).password("new-encoded-password").authorities(List.of()).build(),
                User.withUsername(original.getUsername()).password("old-encoded-password").authorities(List.of()).accountLocked(true).build(),
                User.withUsername(original.getUsername()).password("old-encoded-password").authorities(List.of()).disabled(true).build())) {
            when(users.loadUserByUsername(original.getUsername())).thenReturn(changed);
            assertThrows(BadCredentialsException.class,() -> service.issue(original.getUsername(),"old-plaintext"));
        }
        verifyNoInteractions(encoder);
    }
}
