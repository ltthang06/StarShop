package vn.iotstar.starshop.config;
import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.iotstar.starshop.repository.SecurityUserRepository;
import vn.iotstar.starshop.enums.UserStatus;
@RequiredArgsConstructor
public class ActiveAccountFilter extends OncePerRequestFilter {
    private final SecurityUserRepository users;
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path=request.getServletPath();
        return path.startsWith("/assets/") || path.startsWith("/css/")
                || path.startsWith("/js/") || path.startsWith("/images/") || path.equals("/favicon.ico");
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
        throws IOException,ServletException {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            chain.doFilter(request,response);return;
        }
        if(auth!=null && auth.isAuthenticated()) request.setAttribute("authenticatedEmail",auth.getName());
        var account=auth!=null && auth.isAuthenticated() ? users.findByEmailIgnoreCase(auth.getName()) : java.util.Optional.<vn.iotstar.starshop.entity.User>empty();
        if(auth!=null && auth.getPrincipal() instanceof StarShopPrincipal principal
                && (account.isEmpty() || !principal.matchesCredential(account.get().getPassword()))) {
            SecurityContextHolder.clearContext();
            if(request.getSession(false)!=null) request.getSession(false).invalidate();
            response.sendError(401,"Phiên đăng nhập đã hết hiệu lực. Vui lòng đăng nhập lại");return;
        }
        if(account.isPresent() && account.get().getStatus()!=UserStatus.ACTIVE) {
            SecurityContextHolder.clearContext();
            if(request.getSession(false)!=null) request.getSession(false).invalidate();
            response.sendError(403,"Tài khoản không còn hoạt động");return;
        }
        if(account.isPresent()) {
            var authorities=account.get().getRoles().stream().map(role ->
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_"+role.getName())).toList();
            var updated=org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                auth.getPrincipal(),auth.getCredentials(),authorities);
            updated.setDetails(auth.getDetails());
            SecurityContextHolder.getContext().setAuthentication(updated);
        }
        chain.doFilter(request,response);
    }
}
