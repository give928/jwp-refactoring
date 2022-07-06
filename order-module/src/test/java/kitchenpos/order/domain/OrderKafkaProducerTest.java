package kitchenpos.order.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static kitchenpos.order.OrderFixtures.aOrder1;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://${spring.kafka.bootstrap-servers}", "port=${spring.kafka.port}"})
class OrderKafkaProducerTest {
    @Autowired
    private OrderKafkaProducer orderKafkaProducer;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("주문(메뉴 확인) 이벤트 메시지를 발행한다.")
    @Test
    void sendChangeEmptyOrderTable() {
        // given
        Order order = aOrder1().build();

        // when
        List<OrderMenuMessage> orderMenuMessages = orderKafkaProducer.sendAndReceiveMenusMessage(order.getOrderLineItems());

        // then
        assertThat(orderMenuMessages).hasSameSizeAs(order.getOrderLineItems());
        assertThat(orderMenuMessages).extracting("id")
                .containsExactly(order.getOrderLineItems().stream()
                                         .map(OrderLineItem::getMenuId)
                                         .toArray());
    }

    @DisplayName("주문(테이블 확인) 이벤트 메시지를 발행한다.")
    @Test
    void sendGroupTable() {
        // given
        Order order = aOrder1().build();

        // when
        OrderTableMessage orderTableMessage = orderKafkaProducer.sendAndReceiveExistsAndNotEmptyTableMessage(order);

        // then
        assertThat(orderTableMessage).isNotNull();
        assertThat(orderTableMessage.getOrderTableId()).isEqualTo(order.getOrderTableId());
        assertThat(orderTableMessage.isExists()).isTrue();
        assertThat(orderTableMessage.isEmpty()).isFalse();
    }

    @KafkaListener(topics = "${kafka.topics.get-menus}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String getMenusListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(payload);
        JsonNode menuIds = jsonNode.get("menuIds");
        String jsonMenus = StreamSupport.stream(menuIds.spliterator(), false)
                .map(j -> String.format("{\"id\":%d,\"name\":\"%s\",\"price\":%d}",
                                        j.get("menuId").asLong(),
                                        "음식" + j.get("menuId").asLong(),
                                        j.get("menuId").asLong() * 1_000))
                .collect(Collectors.joining(","));
        String replyMessage = String.format("{\"menuIds\":%s,\"menus\":[%s]}", menuIds, jsonMenus);
        acknowledgment.acknowledge();
        return replyMessage;
    }

    @KafkaListener(topics = "${kafka.topics.exists-and-empty-table}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String existsAndEmptyTableListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        Long orderTableId = objectMapper.readTree(payload).
                get("orderTableId")
                .asLong();
        String replyMessage = String.format("{\"orderTableId\":%d,\"exists\":%s,\"empty\":%s}",
                                            orderTableId,
                                            true,
                                            false);
        acknowledgment.acknowledge();
        return replyMessage;
    }
}
