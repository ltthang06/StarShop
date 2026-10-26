package vn.iotstar.starshop;

import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.*;
import vn.iotstar.starshop.config.*;
import vn.iotstar.starshop.entity.User;
import vn.iotstar.starshop.enums.UserStatus;
import vn.iotstar.starshop.repository.SecurityUserRepository;

class NotificationCredentialTests {
    @Test void passwordChangePreventsOldWebsocketReceivingNewNotifications() throws Exception {
        var users=mock(SecurityUserRepository.class);
        var account=new User();account.setEmail("buyer@example.test");account.setPassword("old-encoded-password");account.setStatus(UserStatus.ACTIVE);
        when(users.findByEmailIgnoreCase(account.getEmail())).thenReturn(Optional.of(account));
        var principal=new StarShopPrincipal(account.getEmail(),account.getPassword(),true,true,List.of());principal.eraseCredentials();
        var auth=new UsernamePasswordAuthenticationToken(principal,"",List.of());
        var session=mock(WebSocketSession.class);
        when(session.getId()).thenReturn("test-session");when(session.getPrincipal()).thenReturn(auth);when(session.isOpen()).thenReturn(true);
        var socket=new NotificationSocket(users);socket.afterConnectionEstablished(session);
        verify(session,times(1)).sendMessage(any(TextMessage.class));
        socket.notifyUser(account.getEmail());verify(session,times(2)).sendMessage(any(TextMessage.class));
        account.setPassword("new-encoded-password");socket.notifyUser(account.getEmail());
        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(session,times(2)).sendMessage(any(TextMessage.class));
        socket.notifyUser(account.getEmail());verify(session,times(1)).close(CloseStatus.POLICY_VIOLATION);
    }
}
