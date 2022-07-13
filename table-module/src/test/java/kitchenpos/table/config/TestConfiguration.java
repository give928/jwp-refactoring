package kitchenpos.table.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.table.domain.KafkaConsumerTest;
import kitchenpos.table.domain.OrderClient;
import kitchenpos.table.dto.OrderLineItemResponse;
import kitchenpos.table.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

@Configuration
public class TestConfiguration {
    public static Boolean UNCOMPLETED_ORDER = Boolean.FALSE;

    @Value("${kafka.topics.order-uncompleted}")
    private String orderUncompletedTopic;

    @Value("${kafka.topics.orders-uncompleted}")
    private String ordersUncompletedTopic;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "${kafka.topics.order-table-changed-empty}")
    protected void orderTableChangedEmptyListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        if (UNCOMPLETED_ORDER) {
            JsonNode jsonNode = objectMapper.readTree(payload);
            kafkaTemplate.send(orderUncompletedTopic,
                               String.format("{\"orderTableId\":%s,\"empty\":%s,\"uncompletedOrderIds\":%s}",
                                             jsonNode.get("orderTableId"), jsonNode.get("empty"), "[1]"));
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "${kafka.topics.table-ungrouped}")
    protected void tableUngroupedListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        if (UNCOMPLETED_ORDER) {
            JsonNode jsonNode = objectMapper.readTree(payload);
            kafkaTemplate.send(ordersUncompletedTopic,
                               String.format("{\"tableGroupId\":%s,\"orderTableIds\":%s,\"uncompletedOrderIds\":%s}",
                                             jsonNode.get("tableGroupId"), jsonNode.get("orderTableIds"), "[1]"));
        }
        acknowledgment.acknowledge();
    }

    @Bean
    public OrderClient orderClient() {
        return new TestOrderClient();
    }

    @Component
    public static class TestOrderClient implements OrderClient {
        @Override
        public OrderResponse getOrder(Long orderId) {
            return new OrderResponse(orderId, KafkaConsumerTest.orderTable.getId(), "COOKING", LocalDateTime.now(),
                                     Collections.singletonList(new OrderLineItemResponse(1L, orderId, 1L, 1)));
        }
    }
}
