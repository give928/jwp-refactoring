package kitchenpos.order.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Component
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final ObjectMapper objectMapper;

    private final OrderRepository orderRepository;

    private final MessageBroadcaster messageBroadcaster;

    public KafkaConsumer(final ObjectMapper objectMapper, final OrderRepository orderRepository,
                         final MessageBroadcaster messageBroadcaster) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.messageBroadcaster = messageBroadcaster;
    }

    @KafkaListener(topics = "${kafka.topics.order-table-changed-empty}")
    protected void orderTableChangedEmptyListener(@Payload String payload, Acknowledgment acknowledgment) {
        log.info("orderTableChangedEmptyListener payload:{}", payload);
        List<Order> uncompletedOrders = null;
        OrderTableChangedEmptyReceivedMessage orderTableChangedEmptyReceivedMessage = null;
        try {
            orderTableChangedEmptyReceivedMessage = objectMapper.readValue(payload, OrderTableChangedEmptyReceivedMessage.class);
            uncompletedOrders = findUncompletedOrders(orderTableChangedEmptyReceivedMessage.getOrderTableId());
            log.info("orderTableChangedEmptyListener order uncompleted orders count:{}", uncompletedOrders.size());
        } catch (JsonProcessingException e) {
            log.error("orderTableChangedEmptyListener catch JsonProcessingException: payload:{}", payload, e);
        } catch (Exception e) {
            log.error("orderTableChangedEmptyListener catch Exception: payload:{}", payload, e);
        }
        if (!CollectionUtils.isEmpty(uncompletedOrders)) {
            messageBroadcaster.broadcast(
                    OrderUncompletedSendingMessage.from(orderTableChangedEmptyReceivedMessage, uncompletedOrders));
        }
        acknowledgment.acknowledge();
    }

    private List<Order> findUncompletedOrders(Long orderTableId) {
        return orderRepository.findByOrderTableIdAndOrderStatusIn(orderTableId, Arrays.asList(OrderStatus.COOKING,
                                                                                              OrderStatus.MEAL));
    }

    @KafkaListener(topics = "${kafka.topics.table-ungrouped}")
    protected void tableUngroupedListener(@Payload String payload, Acknowledgment acknowledgment) {
        log.info("tableUngroupedListener payload:{}", payload);
        List<Order> uncompletedOrders = null;
        TableUngroupedReceivedMessage tableUngroupedReceivedMessage = null;
        try {
            tableUngroupedReceivedMessage = objectMapper.readValue(payload, TableUngroupedReceivedMessage.class);
            uncompletedOrders = findUncompletedOrders(tableUngroupedReceivedMessage.getOrderTableIds());
            log.info("tableUngroupedListener order uncompleted orders count:{}", uncompletedOrders.size());
        } catch (JsonProcessingException e) {
            log.error("tableUngroupedListener catch JsonProcessingException: payload:{}", payload, e);
        } catch (Exception e) {
            log.error("tableUngroupedListener catch Exception: payload:{}", payload, e);
        }
        if (!CollectionUtils.isEmpty(uncompletedOrders)) {
            messageBroadcaster.broadcast(
                    OrdersUncompletedSendingMessage.from(tableUngroupedReceivedMessage, uncompletedOrders));
        }
        acknowledgment.acknowledge();
    }

    private List<Order> findUncompletedOrders(List<Long> orderTableIds) {
        return orderRepository.findByOrderTableIdInAndOrderStatusIn(orderTableIds, Arrays.asList(OrderStatus.COOKING,
                                                                                                 OrderStatus.MEAL));
    }
}
