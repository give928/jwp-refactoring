package kitchenpos.order.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.order.domain.MenuClient;
import kitchenpos.order.dto.MenuResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static kitchenpos.order.OrderFixtures.aOrder1;

@Configuration
public class TestConfiguration {
    public static String ORDER_CREATED_REPLY_MESSAGE = "{\"orderId\":%d,\"orderTableId\":%d,\"exists\":true,\"empty\":false}";

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public MenuClient menuClient() {
        return new TestMenuClient();
    }

    @Component
    public static class TestMenuClient implements MenuClient {
        @Override
        public List<MenuResponse> getMenus(List<Long> menuIds) {
            return menuIds.stream()
                    .map(menuId -> {
                        if (menuId > 0) {
                            return new MenuResponse(menuId, "음식" + menuId, Math.abs(menuId * 1_000));
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    @KafkaListener(topics = "${kafka.topics.order-created}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String orderCreatedListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        acknowledgment.acknowledge();
        JsonNode jsonNode = objectMapper.readTree(payload);
        return String.format(ORDER_CREATED_REPLY_MESSAGE,
                             jsonNode.get("orderId")
                                     .asLong(),
                             aOrder1().build().getOrderTableId());
    }
}
