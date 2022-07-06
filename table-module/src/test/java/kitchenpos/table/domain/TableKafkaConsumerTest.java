package kitchenpos.table.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://${spring.kafka.bootstrap-servers}", "port=${spring.kafka.port}"})
class TableKafkaConsumerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderTableRepository orderTableRepository;

    @Autowired
    private TableKafkaConsumer tableKafkaConsumer;

    @DisplayName("주문 테이블 확인 메시지 스트림 리스너가 주문 테이블의 존재 여부와 빈 테이블 여부를 반환한다.")
    @Test
    void existsAndEmptyTableListener() throws JsonProcessingException {
        // given
        OrderTable orderTable = orderTableRepository.save(OrderTable.of(0, true));
        String payload = String.format("{\"orderTableId\":%d}", orderTable.getId());

        // when
        String replyMessage = tableKafkaConsumer.existsAndEmptyTableListener(payload, acknowledgment);

        // then
        JsonNode jsonNode = objectMapper.readTree(replyMessage);
        assertThat(jsonNode.get("orderTableId").asLong()).isEqualTo(orderTable.getId());
        assertThat(jsonNode.get("exists").asBoolean()).isTrue();
        assertThat(jsonNode.get("empty").asBoolean()).isTrue();
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
