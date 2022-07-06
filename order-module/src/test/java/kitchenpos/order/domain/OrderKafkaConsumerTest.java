package kitchenpos.order.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kitchenpos.order.OrderFixtures.aOrder1;
import static kitchenpos.order.OrderFixtures.aOrder2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://${spring.kafka.bootstrap-servers}", "port=${spring.kafka.port}"})
class OrderKafkaConsumerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderKafkaConsumer orderKafkaConsumer;

    @DisplayName("주문 테이블의 주문 상태 완료 확인 메시지 스트림 리스너가 해당 테이블의 주문 상태 완료 여부를 반환한다.")
    @Test
    void checkOrderStatusCompletionOfTableListener() throws JsonProcessingException {
        // given
        Order order = orderRepository.save(aOrder1().build());
        String payload = String.format("{\"orderTableId\":%d}", order.getOrderTableId());

        // when
        String replyMessage = orderKafkaConsumer.checkOrderStatusCompletionOfTableListener(payload, acknowledgment);

        // then
        JsonNode jsonNode = objectMapper.readTree(replyMessage);
        assertThat(jsonNode.get("orderTableId").asLong()).isEqualTo(order.getOrderTableId());
        assertThat(jsonNode.get("completion").asBoolean()).isFalse();
    }

    @DisplayName("주문 테이블들의 주문 상태 완료 확인 메시지 스트림 리스너가 전체 주문 테이블의 주문 상태 완료 여부를 반환한다.")
    @Test
    void checkOrderStatusCompletionOfTablesListener() throws JsonProcessingException {
        // given
        Order order1 = orderRepository.save(aOrder1().build());
        Order order2 = orderRepository.save(aOrder2().build());
        String payload = String.format("{\"orderTableIds\":[%s]}", Stream.of(order1, order2)
                .map(order -> String.format("{\"orderTableId\":%d}", order.getOrderTableId()))
                .collect(Collectors.joining(",")));

        // when
        String replyMessage = orderKafkaConsumer.checkOrderStatusCompletionOfTablesListener(payload, acknowledgment);

        // then
        JsonNode jsonNode = objectMapper.readTree(replyMessage);
        assertThat(jsonNode.get("orderTableIds").get(0).get("orderTableId").asLong()).isEqualTo(order1.getOrderTableId());
        assertThat(jsonNode.get("orderTableIds").get(1).get("orderTableId").asLong()).isEqualTo(order2.getOrderTableId());
        assertThat(jsonNode.get("completion").asBoolean()).isFalse();
    }

    private final Acknowledgment acknowledgment = new Acknowledgment() {
        @Override
        public void acknowledge() {
        }

        @Override
        public void nack(long sleep) {
            Acknowledgment.super.nack(sleep);
        }

        @Override
        public void nack(int index, long sleep) {
            Acknowledgment.super.nack(index, sleep);
        }
    };
}
