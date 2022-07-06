package kitchenpos.table.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.table.exception.OrderTableMessageStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TableKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(TableKafkaConsumer.class);

    private static final String ORDER_TABLE_ID_JSON_FIELD_NAME = "orderTableId";
    private static final String REPLY_EXISTS_AND_NOT_EMPTY_TABLE_MESSAGE_FORMAT = "{\"orderTableId\":%d,\"exists\":%s,\"empty\":%s}";

    private final ObjectMapper objectMapper;
    private final OrderTableRepository orderTableRepository;

    public TableKafkaConsumer(ObjectMapper objectMapper, OrderTableRepository orderTableRepository) {
        this.objectMapper = objectMapper;
        this.orderTableRepository = orderTableRepository;
    }

    @KafkaListener(topics = "${kafka.topics.exists-and-empty-table}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String existsAndEmptyTableListener(@Payload String payload, Acknowledgment acknowledgment) {
        log.info("TableKafkaConsumer.checkTableByOrderEventListener payload:{}", payload);
        Long orderTableId = mapOrderTableId(payload);
        Optional<OrderTable> orderTable = orderTableRepository.findById(orderTableId);
        String replyMessage = String.format(REPLY_EXISTS_AND_NOT_EMPTY_TABLE_MESSAGE_FORMAT,
                                            orderTableId,
                                            orderTable.isPresent(),
                                            orderTable.filter(OrderTable::isEmpty)
                                                    .isPresent());
        acknowledgment.acknowledge();
        return replyMessage;
    }

    private Long mapOrderTableId(String payload) {
        try {
            return Optional.of(objectMapper.readTree(payload).
                                       get(ORDER_TABLE_ID_JSON_FIELD_NAME)
                                       .asLong())
                    .filter(l -> l > 0)
                    .orElseThrow(() -> new OrderTableMessageStreamException(payload));
        } catch (JsonProcessingException e) {
            throw new OrderTableMessageStreamException(payload);
        }
    }
}
