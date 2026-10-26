package vn.iotstar.starshop.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.starshop.service.FlowerChatService;
import vn.iotstar.starshop.service.FlowerChatGateway.ChatUnavailableException;

@RestController
@RequestMapping("/flower-chat")
public class FlowerChatController {
    private final FlowerChatService service;
    public FlowerChatController(FlowerChatService service) { this.service = service; }
    public record Question(@NotBlank @Size(max = 1000) String message) {}

    @PostMapping
    public ResponseEntity<?> chat(@Valid @RequestBody Question question, HttpSession session) {
        List<String> history;
        synchronized (session) {
            Long last = (Long) session.getAttribute("flowerChatLast");
            if (Boolean.TRUE.equals(session.getAttribute("flowerChatBusy"))
                    || (last != null && System.currentTimeMillis() - last < 10000)) {
                return ResponseEntity.status(429).body(Map.of("message", "Bạn vui lòng chờ 10 giây giữa các câu hỏi."));
            }
            session.setAttribute("flowerChatBusy", true);
            session.setAttribute("flowerChatLast", System.currentTimeMillis());
            Object saved = session.getAttribute("flowerChatHistory");
            history = new ArrayList<>();
            if (saved instanceof List<?> list) {
                for (Object value : list) if (value instanceof String text) history.add(text);
            }
        }
        try {
            var reply = service.reply(question.message(), history);
            synchronized (session) {
                history.add("Khách: " + question.message());
                history.add("Tư vấn: " + reply.reply());
                while (history.size() > 6) history.remove(0);
                session.setAttribute("flowerChatHistory", List.copyOf(history));
            }
            return ResponseEntity.ok(reply);
        } catch (ChatUnavailableException ex) {
            return ResponseEntity.status(ex.getStatus()).body(Map.of("message", ex.getMessage()));
        } finally {
            synchronized (session) { session.removeAttribute("flowerChatBusy"); }
        }
    }
}
