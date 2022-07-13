package kitchenpos.table.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.common.exception.KafkaMessageReceiveException;
import kitchenpos.table.dto.OrderResponse;
import kitchenpos.table.exception.OrderTableNotFoundException;
import kitchenpos.table.exception.TableGroupNotFoundException;
import kitchenpos.table.exception.TableGroupRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private static final String ORDER_ID_JSON_FIELD_NAME = "orderId";
    protected static final String REPLY_ORDER_CREATED_MESSAGE_FORMAT = "{\"orderId\":%d,\"orderTableId\":%d,\"exists\":%s,\"empty\":%s}";

    private final ObjectMapper objectMapper;
    private final OrderTableRepository orderTableRepository;
    private final TableGroupRepository tableGroupRepository;
    private final OrderClient orderClient;

    public KafkaConsumer(ObjectMapper objectMapper, OrderTableRepository orderTableRepository,
                         TableGroupRepository tableGroupRepository, OrderClient orderClient) {
        this.objectMapper = objectMapper;
        this.orderTableRepository = orderTableRepository;
        this.tableGroupRepository = tableGroupRepository;
        this.orderClient = orderClient;
    }

    @KafkaListener(topics = "${kafka.topics.order-created}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String orderCreatedListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        log.info("orderCreatedListener payload:{}", payload);
        long orderId = parseOrderId(payload);
        OrderResponse orderResponse =  orderClient.getOrder(orderId);
        log.info("orderCreatedListener orderClient orderId:{}, orderTableId:{}", orderId, orderResponse.getOrderTableId());
        String replyMessage = makeOrderCreatedReplyMessage(orderId, orderResponse.getOrderTableId());
        log.info("orderCreatedListener replyMessage:{}", replyMessage);
        acknowledgment.acknowledge();
        return replyMessage;
    }

    private long parseOrderId(String payload) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(payload);
        return Optional.ofNullable(jsonNode.get(ORDER_ID_JSON_FIELD_NAME))
                .orElseThrow(() -> new KafkaMessageReceiveException(payload))
                .asLong();
    }

    private String makeOrderCreatedReplyMessage(long orderId, long orderTableId) {
        Optional<OrderTable> orderTable = orderTableRepository.findById(orderTableId);
        return String.format(REPLY_ORDER_CREATED_MESSAGE_FORMAT,
                             orderId,
                             orderTableId,
                             orderTable.isPresent(),
                             orderTable.filter(OrderTable::isEmpty)
                                     .isPresent());
    }

    @KafkaListener(topics = "${kafka.topics.order-uncompleted}")
    protected void orderUncompletedListener(@Payload String payload, Acknowledgment acknowledgment) {
        log.info("orderUncompletedListener payload:{}", payload);
        try {
            OrderUncompletedReceivedMessage message = objectMapper.readValue(payload, OrderUncompletedReceivedMessage.class);
            rollbackEmpty(message);
        } catch (JsonProcessingException e) {
            throw new KafkaMessageReceiveException(payload, e);
        }
        acknowledgment.acknowledge();
    }

    @Transactional
    public void rollbackEmpty(OrderUncompletedReceivedMessage message) {
        log.info("rollbackEmpty orderTableId:{}, empty:{}, orderIds:{}", message.getOrderTableId(), message.isEmpty(), message.getUncompletedOrderIds());
        OrderTable orderTable = orderTableRepository.findById(message.getOrderTableId())
                .orElseThrow(OrderTableNotFoundException::new);
        orderTableRepository.save(orderTable.rollbackEmpty(message.isEmpty()));
    }

    @KafkaListener(topics = "${kafka.topics.orders-uncompleted}")
    protected void ordersUncompletedListener(@Payload String payload, Acknowledgment acknowledgment) {
        log.info("ordersUncompletedListener payload:{}", payload);
        try {
            OrdersUncompletedReceivedMessage message = objectMapper.readValue(payload, OrdersUncompletedReceivedMessage.class);
            rollbackTableGroup(message);
        } catch (JsonProcessingException e) {
            throw new KafkaMessageReceiveException(payload, e);
        }
        acknowledgment.acknowledge();
    }

    @Transactional
    public void rollbackTableGroup(OrdersUncompletedReceivedMessage message) {
        log.info("rollbackTableGroup tableGroupId:{}, orderTableIds:{}, orderIds:{}", message.getTableGroupId(), message.getOrderTableIds(), message.getUncompletedOrderIds());
        TableGroup tableGroup = tableGroupRepository.findById(message.getTableGroupId())
                .orElseThrow(TableGroupNotFoundException::new);
        List<OrderTable> orderTables = orderTableRepository.findAllByIdIn(message.getOrderTableIds());
        validateOrderTables(message, orderTables);
        tableGroup.group(orderTables);
        tableGroupRepository.save(tableGroup);
    }

    private void validateOrderTables(OrdersUncompletedReceivedMessage message, List<OrderTable> orderTables) {
        if (orderTables.size() != message.getOrderTableIds().size()) {
            throw new OrderTableNotFoundException();
        }
        boolean otherTableGroup = orderTables.stream()
                .anyMatch(orderTable -> orderTable.getTableGroup() != null &&
                        !Objects.equals(orderTable.getTableGroup().getId(), message.getTableGroupId()));
        log.info("rollbackTableGroup validateOrderTables tableGroupId:{}, orderTableIds:{}, otherTableGroup:{}", message.getTableGroupId(), message.getOrderTableIds(), otherTableGroup);
        if (otherTableGroup) {
            throw new TableGroupRollbackException();
        }
    }
}
