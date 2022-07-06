package kitchenpos.order.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.order.exception.OrderStatusMessageStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class OrderKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderKafkaConsumer.class);

    private static final String ORDER_TABLE_IDS_JSON_FIELD_NAME = "orderTableIds";
    private static final String ORDER_TABLE_ID_JSON_FIELD_NAME = "orderTableId";
    private static final String REPLY_CHECK_ORDER_STATUS_COMPLETION_OF_TABLE_MESSAGE_FORMAT = "{\"orderTableId\":%d,\"completion\":%s}";
    private static final String REPLY_CHECK_ORDER_STATUS_COMPLETION_OF_TABLES_MESSAGE_FORMAT = "{\"orderTableIds\":[%s],\"completion\":%s}";
    public static final String REPLY_CHECK_ORDER_STATUS_COMPLETION_OF_TABLES_TABLE_ID_MESSAGE_FORMAT = "{\"orderTableId\":%d}";

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    public OrderKafkaConsumer(final ObjectMapper objectMapper, final OrderRepository orderRepository) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "${kafka.topics.check-order-status-completion-of-table}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String checkOrderStatusCompletionOfTableListener(@Payload String payload, Acknowledgment acknowledgment) {
        log.info("OrderKafkaConsumer.checkOrderStatusCompletionOfTableListener payload:{}", payload);
        Long orderTableId = mapOrderTableId(payload);
        String replyMessage = String.format(REPLY_CHECK_ORDER_STATUS_COMPLETION_OF_TABLE_MESSAGE_FORMAT,
                                            orderTableId,
                                            !existsOrderStatusInCookingOrMeal(orderTableId));
        log.info("OrderKafkaConsumer.checkOrderStatusCompletionOfTableListener replyMessage:{}", replyMessage);
        acknowledgment.acknowledge();
        return replyMessage;
    }

    private Long mapOrderTableId(String payload) {
        try {
            return Optional.of(objectMapper.readTree(payload)
                                       .get(ORDER_TABLE_ID_JSON_FIELD_NAME)
                                       .asLong())
                    .filter(l -> l > 0)
                    .orElseThrow(() -> new OrderStatusMessageStreamException(payload));
        } catch (JsonProcessingException e) {
            throw new OrderStatusMessageStreamException(payload);
        }
    }

    private boolean existsOrderStatusInCookingOrMeal(Long orderTableId) {
        return orderRepository.existsByOrderTableIdAndOrderStatusIn(orderTableId, Arrays.asList(OrderStatus.COOKING,
                                                                                                OrderStatus.MEAL));
    }

    @KafkaListener(topics = "${kafka.topics.check-order-status-completion-of-tables}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String checkOrderStatusCompletionOfTablesListener(@Payload String payload,
                                                                Acknowledgment acknowledgment) {
        log.info("OrderKafkaConsumer.checkOrderStatusCompletionOfTablesListener payload:{}", payload);
        List<Long> orderTableIds = mapOrderTableIds(payload);
        String replyMessage = String.format(REPLY_CHECK_ORDER_STATUS_COMPLETION_OF_TABLES_MESSAGE_FORMAT,
                                            orderTableIds
                                                    .stream()
                                                    .map(orderTableId -> String.format(
                                                            REPLY_CHECK_ORDER_STATUS_COMPLETION_OF_TABLES_TABLE_ID_MESSAGE_FORMAT,
                                                            orderTableId))
                                                    .collect(Collectors.joining(",")),
                                            !existsOrderStatusInCookingOrMeal(orderTableIds));
        log.info("OrderKafkaConsumer.checkOrderStatusCompletionOfTablesListener replyMessage:{}", replyMessage);
        acknowledgment.acknowledge();
        return replyMessage;
    }

    private List<Long> mapOrderTableIds(String payload) {
        try {
            List<Long> orderTableIds = StreamSupport.stream(objectMapper.readTree(payload)
                                                              .get(ORDER_TABLE_IDS_JSON_FIELD_NAME)
                                                              .spliterator(),
                                                      false)
                    .map(jsonNode -> jsonNode.get(ORDER_TABLE_ID_JSON_FIELD_NAME).asLong())
                    .sorted()
                    .collect(Collectors.toList());
            return Optional.of(orderTableIds)
                    .filter(l -> !l.isEmpty())
                    .orElseThrow(() -> new OrderStatusMessageStreamException(payload));
        } catch (JsonProcessingException e) {
            throw new OrderStatusMessageStreamException(payload);
        }
    }

    private boolean existsOrderStatusInCookingOrMeal(List<Long> orderTableIds) {
        return orderRepository.existsByOrderTableIdInAndOrderStatusIn(orderTableIds, Arrays.asList(OrderStatus.COOKING,
                                                                                                   OrderStatus.MEAL));
    }
}
