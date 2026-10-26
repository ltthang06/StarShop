package vn.iotstar.starshop.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import vn.iotstar.starshop.config.GeminiProperties;
import vn.iotstar.starshop.entity.*;
import vn.iotstar.starshop.repository.CustomerCatalogRepository;
import vn.iotstar.starshop.service.FlowerChatGateway.ChatUnavailableException;

class FlowerChatServiceTests {
    GeminiProperties properties;
    CustomerCatalogRepository catalog;
    FlowerChatGateway gateway;
    FlowerChatService service;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach void setup() throws Exception {
        properties = new GeminiProperties(); properties.setEnabled(true); properties.setApiKey("private-test-key");
        catalog = mock(CustomerCatalogRepository.class); gateway = mock(FlowerChatGateway.class);
        var transactions = mock(PlatformTransactionManager.class);
        when(transactions.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        service = new FlowerChatService(properties, catalog, gateway, mapper, transactions,
                Clock.fixed(Instant.parse("2026-10-25T03:00:00Z"), ZoneOffset.UTC));
        var category = new Category(); category.setName("Hoa sinh nhật");
        var shop = new Shop(); shop.setName("StarShop");
        var product = new Product(); product.setId(12L); product.setName("Hoa hồng");
        product.setPrice(new BigDecimal("700000")); product.setDiscountPrice(new BigDecimal("600000"));
        product.setCategory(category); product.setShop(shop);
        when(catalog.findChatCatalog(any())).thenReturn(List.of(product));
        when(gateway.ask(anyString())).thenReturn(mapper.readTree("{\"reply\":\"Mời bạn xem mẫu này\",\"productIds\":[12,999,12,\"12\"]}"));
    }

    @Test void keepsOnlyExistingDistinctProductsAndActualDiscountPrice() {
        var reply = service.reply("Hoa tặng mẹ", List.of());
        assertThat(reply.products()).hasSize(1);
        assertThat(reply.products().get(0).id()).isEqualTo(12L);
        assertThat(reply.products().get(0).price()).isEqualByComparingTo("600000");
        verify(gateway).ask(argThat(prompt -> prompt.contains("600000") && !prompt.contains("private-test-key")));
    }

    @Test void rejectsUnavailableAndInvalidInputWithoutCallingProvider() {
        assertThatThrownBy(() -> service.reply("x".repeat(1001), List.of())).isInstanceOf(ChatUnavailableException.class);
        properties.setEnabled(false);
        assertThatThrownBy(() -> service.reply("Hoa", List.of())).isInstanceOf(ChatUnavailableException.class);
        verifyNoInteractions(gateway);
    }

    @Test void enforcesGlobalMinuteQuota() {
        properties.setRequestsPerMinute(2);
        service.reply("Hoa", List.of()); service.reply("Hoa", List.of());
        assertThatThrownBy(() -> service.reply("Hoa", List.of())).isInstanceOfSatisfying(ChatUnavailableException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(429));
        verify(gateway, times(2)).ask(anyString());
    }

    @Test void countsFailedProviderCallsAgainstDailyBudget() {
        properties.setDailyLimit(1);
        when(gateway.ask(anyString())).thenThrow(new ChatUnavailableException("Bận", 503));
        assertThatThrownBy(() -> service.reply("Hoa", List.of())).isInstanceOf(ChatUnavailableException.class);
        assertThatThrownBy(() -> service.reply("Hoa", List.of())).isInstanceOfSatisfying(ChatUnavailableException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(429));
        verify(gateway, times(1)).ask(anyString());
    }

    @Test void emptyCatalogDoesNotSpendQuota() {
        when(catalog.findChatCatalog(any())).thenReturn(List.of());
        assertThat(service.reply("Hoa", List.of()).products()).isEmpty();
        verifyNoInteractions(gateway);
    }
}
