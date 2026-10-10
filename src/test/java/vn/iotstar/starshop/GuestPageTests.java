package vn.iotstar.starshop;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.ServletContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GuestPageTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ServletContext servletContext;

    @Test
    void publicPagesRender() throws Exception {
        assertThat(servletContext.getResource(
                "/WEB-INF/views/guest/home.jsp")).isNotNull();

        String[] paths = {
                "/",
                "/products",
                "/login",
                "/register",
                "/verify-otp?email=customer%40example.com",
                "/forgot-password",
                "/reset-password"
        };

        for (String path : paths) {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    path,
                    String.class
            );
            assertThat(response.getStatusCode().is2xxSuccessful())
                    .as(path + ": " + response.getStatusCode()
                            + " " + response.getBody())
                    .isTrue();
        }
    }
}
