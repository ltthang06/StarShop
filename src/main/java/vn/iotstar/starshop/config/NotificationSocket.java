package vn.iotstar.starshop.config;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.repository.SecurityUserRepository;
import vn.iotstar.starshop.enums.UserStatus;
@Component @RequiredArgsConstructor
public class NotificationSocket extends TextWebSocketHandler {
    private final Map<String,WebSocketSession> sessions=new ConcurrentHashMap<>();
    private final SecurityUserRepository users;
    @Override public void afterConnectionEstablished(WebSocketSession raw) throws Exception {
        if (raw.getPrincipal()==null || !active(raw.getPrincipal().getName())) {raw.close(CloseStatus.POLICY_VIOLATION);return;}
        sessions.put(raw.getId(),new ConcurrentWebSocketSessionDecorator(raw,5000,8192));
        raw.sendMessage(new TextMessage("{\"event\":\"ready\"}"));
    }
    @Override public void afterConnectionClosed(WebSocketSession session,CloseStatus status) {sessions.remove(session.getId());}
    @Override protected void handleTextMessage(WebSocketSession session,TextMessage message) throws Exception {
        session.close(CloseStatus.POLICY_VIOLATION);
    }
    public void notifyUser(String email) {
        for (WebSocketSession session: sessions.values()) {
            if (session.getPrincipal()!=null && email.equalsIgnoreCase(session.getPrincipal().getName())) {
                try {
                    if (!active(email)) {session.close(CloseStatus.POLICY_VIOLATION);sessions.remove(session.getId());continue;}
                    if (session.isOpen()) session.sendMessage(new TextMessage("{\"event\":\"notification\"}"));
                } catch (Exception ex) {sessions.remove(session.getId());}
            }
        }
    }
    private boolean active(String email) {
        return users.findByEmailIgnoreCase(email).map(u -> u.getStatus()==UserStatus.ACTIVE).orElse(false);
    }
}
