package kitchenpos.table.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.table.exception.OrderStatusMessageStreamException;
import kitchenpos.table.exception.OrdersStatusMessageStreamException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class TableKafkaProducer implements TableEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(TableKafkaProducer.class);


    private static final String ORDER_TABLE_ID_JSON_FIELD_NAME = "orderTableId";
    private static final String ORDER_TABLE_IDS_JSON_FIELD_NAME = "orderTableIds";
    private static final String ORDER_STATUS_COMPLETION_JSON_FIELD_NAME = "completion";
    private static final String CHECK_ORDER_STATUS_COMPLETION_BY_TABLE_EVENT_MESSAGE_FORMAT = "{\"orderTableId\":%d}";
    private static final String CHECK_ORDER_STATUS_COMPLETION_BY_TABLES_EVENT_MESSAGE_FORMAT = "{\"orderTableIds\":[%s]}";

    @Value("${kafka.topics.check-order-status-completion-of-table}")
    private String checkOrderStatusCompletionOfTableTopic;

    @Value("${kafka.topics.reply-check-order-status-completion-of-table}")
    private String replyCheckOrderStatusCompletionOfTableTopic;

    @Value("${kafka.topics.check-order-status-completion-of-tables}")
    private String checkOrderStatusCompletionOfTablesTopic;

    @Value("${kafka.topics.reply-check-order-status-completion-of-tables}")
    private String replyCheckOrderStatusCompletionOfTablesTopic;

    private final ObjectMapper objectMapper;

    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    public TableKafkaProducer(ObjectMapper objectMapper,
                              ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate) {
        this.objectMapper = objectMapper;
        this.replyingKafkaTemplate = replyingKafkaTemplate;
    }

    @Override
    public boolean sendOrderTableEmptyChangeMessage(OrderTable orderTable) {
        log.info("TableKafkaProducer.sendOrderTableEmptyChangeMessage id:{}, empty:{}", orderTable.getId(),
                 orderTable.isEmpty());
        String sendMessage = null;
        String receiveMessage = null;
        try {
            sendMessage = String.format(CHECK_ORDER_STATUS_COMPLETION_BY_TABLE_EVENT_MESSAGE_FORMAT,
                                        orderTable.getId());
            log.info("TableKafkaProducer.sendOrderTableEmptyChangeMessage sendMessage:{}", sendMessage);
            receiveMessage = sendAndReceive(checkOrderStatusCompletionOfTableTopic,
                                            replyCheckOrderStatusCompletionOfTableTopic, sendMessage);
            log.info("TableKafkaProducer.sendOrderTableEmptyChangeMessage receiveMessage:{}", receiveMessage);
            return isOrderStatusCompletion(orderTable, receiveMessage);
        } catch (JsonProcessingException | OrderStatusMessageStreamException | ExecutionException | TimeoutException e) {
            throw new OrderStatusMessageStreamException(sendMessage, receiveMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderStatusMessageStreamException(sendMessage, receiveMessage);
        }
    }

    private boolean isOrderStatusCompletion(OrderTable orderTable, String receiveMessage) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(receiveMessage);
        validateReceiveMessageOrderTableId(orderTable, jsonNode);
        return jsonNode
                .get(ORDER_STATUS_COMPLETION_JSON_FIELD_NAME)
                .asBoolean();
    }

    private void validateReceiveMessageOrderTableId(OrderTable orderTable, JsonNode jsonNode) {
        long receiveOrderTableId = jsonNode.get(ORDER_TABLE_ID_JSON_FIELD_NAME).asLong();
        if (receiveOrderTableId != orderTable.getId()) {
            throw new OrderStatusMessageStreamException();
        }
    }

    @Override
    public boolean sendGroupTableMessage(TableGroup tableGroup) {
        log.info("TableKafkaProducer.sendGroupTableMessage id:{}, orderTables:{}", tableGroup.getId(),
                 tableGroup.getOrderTables());
        String sendMessage = null;
        String receiveMessage = null;
        try {
            sendMessage = String.format(CHECK_ORDER_STATUS_COMPLETION_BY_TABLES_EVENT_MESSAGE_FORMAT,
                                        tableGroup.getOrderTables()
                                                .stream()
                                                .map(orderTable -> String.format(
                                                        CHECK_ORDER_STATUS_COMPLETION_BY_TABLE_EVENT_MESSAGE_FORMAT,
                                                        orderTable.getId()))
                                                .collect(Collectors.joining(",")));
            log.info("TableKafkaProducer.sendGroupTableMessage sendMessage:{}", sendMessage);
            receiveMessage = sendAndReceive(checkOrderStatusCompletionOfTablesTopic,
                                            replyCheckOrderStatusCompletionOfTablesTopic, sendMessage);
            log.info("TableKafkaProducer.sendGroupTableMessage receiveMessage:{}", receiveMessage);
            return isOrderStatusCompletion(tableGroup, receiveMessage);
        } catch (JsonProcessingException | ExecutionException | TimeoutException e) {
            throw new OrdersStatusMessageStreamException(sendMessage, receiveMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrdersStatusMessageStreamException(sendMessage, receiveMessage);
        }
    }

    private boolean isOrderStatusCompletion(TableGroup tableGroup, String receiveMessage) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(receiveMessage);
        validateReceiveMessageOrderTableId(tableGroup, jsonNode);
        return jsonNode
                .get(ORDER_STATUS_COMPLETION_JSON_FIELD_NAME)
                .asBoolean();
    }

    private void validateReceiveMessageOrderTableId(TableGroup tableGroup, JsonNode jsonNode) {
        List<Long> receiveOrderTableIds = StreamSupport.stream(jsonNode
                                                          .get(ORDER_TABLE_IDS_JSON_FIELD_NAME)
                                                          .spliterator(),
                                                  false)
                .map(j -> j.get(ORDER_TABLE_ID_JSON_FIELD_NAME).asLong())
                .sorted()
                .collect(Collectors.toList());
        List<Long> orderTableIds = tableGroup.getOrderTables()
                .stream()
                .map(OrderTable::getId)
                .sorted()
                .collect(Collectors.toList());
        if (!orderTableIds.equals(receiveOrderTableIds)) {
            throw new OrderStatusMessageStreamException();
        }
    }

    private String sendAndReceive(String topic, String replyTopic, String message)
            throws ExecutionException, InterruptedException, TimeoutException {
        log.info("TableKafkaProducer.sendAndReceive topic:{}, replyTopic:{}, message:{}", topic, replyTopic, message);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, message);
        producerRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes()));
        RequestReplyFuture<String, String, String> requestReplyFuture = replyingKafkaTemplate.sendAndReceive(producerRecord);
        ConsumerRecord<String, String> consumerRecord = requestReplyFuture.get(10, TimeUnit.SECONDS);
        return consumerRecord.value();
    }
}
