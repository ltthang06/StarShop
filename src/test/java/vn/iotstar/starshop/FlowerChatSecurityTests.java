package vn.iotstar.starshop;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import vn.iotstar.starshop.service.FlowerChatService;

@SpringBootTest
@AutoConfigureMockMvc
class FlowerChatSecurityTests {
    @Autowired MockMvc mvc;
    @MockBean FlowerChatService chat;

    @Test void guestRequiresCsrfAndHasSessionCooldown() throws Exception {
        when(chat.reply(anyString(), anyList())).thenReturn(new FlowerChatService.Reply("Bạn thích hoa màu gì?", List.of()));
        mvc.perform(post("/flower-chat").contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"Hoa cho mẹ\"}"))
                .andExpect(status().isForbidden());
        var session = new MockHttpSession();
        mvc.perform(post("/flower-chat").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Hoa cho mẹ\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reply").value("Bạn thích hoa màu gì?"));
        mvc.perform(post("/flower-chat").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Tiếp nhé\"}"))
                .andExpect(status().isTooManyRequests());
        verify(chat, times(1)).reply(anyString(), anyList());
    }

    @Test void rejectsLongQuestionsBeforeProvider() throws Exception {
        mvc.perform(post("/flower-chat").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + "x".repeat(1001) + "\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(chat);
    }
}
