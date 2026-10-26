package vn.iotstar.starshop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.iotstar.starshop.config.ActiveAccountFilter;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.SecurityUserRepository;

class ActiveAccountFilterTests {
    private final SecurityUserRepository users=mock(SecurityUserRepository.class);
    @AfterEach void clear() {SecurityContextHolder.clearContext();}

    @Test void guestAndPublicAssetsDoNotReadAccountDatabase() throws Exception {
        var filter=new ActiveAccountFilter(users);
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("test","anonymousUser",List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        var guestRequest=new MockHttpServletRequest("GET","/products");
        var chain=new MockFilterChain();filter.doFilter(guestRequest,new MockHttpServletResponse(),chain);
        assertNotNull(chain.getRequest());assertNull(guestRequest.getAttribute("authenticatedEmail"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("buyer@example.test","unused",List.of()));
        var asset=new MockHttpServletRequest("GET","/images/catalog/vuonhoatuoi/vht-001.webp");
        asset.setServletPath("/images/catalog/vuonhoatuoi/vht-001.webp");
        var assetChain=new MockFilterChain();filter.doFilter(asset,new MockHttpServletResponse(),assetChain);
        assertNotNull(assetChain.getRequest());verifyNoInteractions(users);
    }

    @Test void lockedAccountStillLosesSessionAndCannotReachProtectedHandler() throws Exception {
        var user=new User();user.setStatus(UserStatus.LOCKED);
        when(users.findByEmailIgnoreCase("buyer@example.test")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("buyer@example.test","unused",List.of()));
        var request=new MockHttpServletRequest("GET","/orders");request.getSession();
        var response=new MockHttpServletResponse();var chain=new MockFilterChain();
        new ActiveAccountFilter(users).doFilter(request,response,chain);
        assertEquals(403,response.getStatus());assertNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());assertNull(request.getSession(false));
    }

    @Test void deletedAccountCannotKeepRealLoginSession() throws Exception {
        var principal=new vn.iotstar.starshop.config.StarShopPrincipal("deleted@example.test","encoded-password",true,true,List.of());
        principal.eraseCredentials();
        when(users.findByEmailIgnoreCase(principal.getUsername())).thenReturn(Optional.empty());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal,"",List.of()));
        var request=new MockHttpServletRequest("GET","/profile");request.getSession();
        var response=new MockHttpServletResponse();var chain=new MockFilterChain();
        new ActiveAccountFilter(users).doFilter(request,response,chain);
        assertEquals(401,response.getStatus());assertNull(chain.getRequest());assertNull(request.getSession(false));
    }
}
