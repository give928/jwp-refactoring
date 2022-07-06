package kitchenpos.order.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        Boolean result = orderKafkaProducer.sendAndReceiveExistsMenusMessage(order);

        // then
        assertThat(result).isNotNull();
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

    @KafkaListener(topics = "${kafka.topics.exists-menus}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String existsMenusListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        String replyMessage = String.format("{\"menuIds\":%s,\"exists\":%s}",
                                            objectMapper.readTree(payload)
                                                    .get("menuIds")
                                                    .toString(),
                                            true);
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
