package kitchenpos.table.domain;

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

import static kitchenpos.table.TableFixtures.aOrderTable1;
import static kitchenpos.table.TableFixtures.aTableGroup1;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://${spring.kafka.bootstrap-servers}", "port=${spring.kafka.port}"})
class TableKafkaProducerTest {
    @Autowired
    private TableKafkaProducer tableKafkaProducer;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("주문 테이블 빈 테이블 여부 변경 이벤트 메시지를 발행한다.")
    @Test
    void sendChangeEmptyOrderTable() {
        // given
        OrderTable orderTable = aOrderTable1();

        // when
        Boolean result = tableKafkaProducer.sendOrderTableEmptyChangeMessage(orderTable);

        // then
        assertThat(result).isNotNull();
    }

    @DisplayName("단체 지정 이벤트 메시지를 발행한다.")
    @Test
    void sendGroupTable() {
        // given
        TableGroup tableGroup = aTableGroup1();

        // when
        Boolean result = tableKafkaProducer.sendGroupTableMessage(tableGroup);

        // then
        assertThat(result).isNotNull();
    }

    @KafkaListener(topics = "${kafka.topics.check-order-status-completion-of-table}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String checkOrderStatusCompletionOfTableListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        acknowledgment.acknowledge();
        return String.format("{\"orderTableId\":%d,\"completion\":true}",
                             objectMapper.readTree(payload)
                                     .get("orderTableId")
                                     .asLong());
    }

    @KafkaListener(topics = "${kafka.topics.check-order-status-completion-of-tables}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String checkOrderStatusCompletionOfTablesListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        acknowledgment.acknowledge();
        return String.format("{\"orderTableIds\":%s,\"completion\":true}",
                             objectMapper.readTree(payload)
                                     .get("orderTableIds")
                                     .toString());
    }
}
