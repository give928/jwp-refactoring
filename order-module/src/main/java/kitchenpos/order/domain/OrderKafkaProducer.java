package kitchenpos.order.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.order.exception.OrderLineItemMessageStreamException;
import kitchenpos.order.exception.OrderTableMessageStreamException;
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
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
public class OrderKafkaProducer implements OrderEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderKafkaProducer.class);
    private static final String EXISTS_AND_NOT_EMPTY_TABLE_MESSAGE_FORMAT = "{\"orderTableId\":%d}";
    private static final String CHECK_MENU_BY_ORDER_EVENT_MESSAGE_FORMAT = "{\"menuId\":%d}";
    private static final String EXISTS_MENUS_MESSAGE_FORMAT = "{\"menuIds\":[%s]}";

    @Value("${kafka.topics.exists-and-empty-table}")
    private String existsAndEmptyTableTopic;

    @Value("${kafka.topics.reply-exists-and-empty-table}")
    private String replyExistsAndNotEmptyTableTopic;

    @Value("${kafka.topics.get-menus}")
    private String getMenusTopic;

    @Value("${kafka.topics.reply-get-menus}")
    private String replyExistsMenusTopic;

    private final ObjectMapper objectMapper;

    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    public OrderKafkaProducer(ObjectMapper objectMapper, ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate) {
        this.objectMapper = objectMapper;
        this.replyingKafkaTemplate = replyingKafkaTemplate;
    }

    @Override
    public List<OrderMenuMessage> sendAndReceiveMenusMessage(List<OrderLineItem> orderLineItems) {
        log.info("OrderKafkaProducer.sendAndReceiveMenusMessage orderLineItems:{}", orderLineItems);
        String receiveMessage = null;
        String sendMessage = null;
        try {
            sendMessage = String.format(EXISTS_MENUS_MESSAGE_FORMAT, orderLineItems
                    .stream()
                    .map(orderLineItem -> String.format(CHECK_MENU_BY_ORDER_EVENT_MESSAGE_FORMAT,
                                                        orderLineItem.getMenuId()))
                    .collect(Collectors.joining(",")));
            log.info("OrderKafkaProducer.sendAndReceiveMenusMessage sendMessage:{}", sendMessage);
            receiveMessage = sendAndReceive(getMenusTopic, replyExistsMenusTopic, sendMessage);
            log.info("OrderKafkaProducer.sendAndReceiveMenusMessage receiveMessage:{}", receiveMessage);
            return mapOrderMenus(receiveMessage);
        } catch (IllegalStateException | JsonProcessingException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            throw new OrderLineItemMessageStreamException(sendMessage, receiveMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderLineItemMessageStreamException(sendMessage, receiveMessage);
        }
    }

    private List<OrderMenuMessage> mapOrderMenus(String receiveMessage) throws JsonProcessingException {
        return objectMapper.readValue(objectMapper.readTree(receiveMessage)
                                              .withArray("menus")
                                              .toString(),
                                      new TypeReference<>(){});
    }

    @Override
    public OrderTableMessage sendAndReceiveExistsAndNotEmptyTableMessage(Order order) {
        log.info("OrderKafkaProducer.sendAndReceiveExistsAndNotEmptyTableMessage id:{}, orderTableId:{}, orderStatus:{}, orderedTime:{}, orderLineItems:{}", order.getId(), order.getOrderTableId(), order.getOrderStatus(), order.getOrderedTime(), order.getOrderLineItems());
        String receiveMessage = null;
        String sendMessage = null;
        try {
            sendMessage = String.format(EXISTS_AND_NOT_EMPTY_TABLE_MESSAGE_FORMAT, order.getOrderTableId());
            log.info("OrderKafkaProducer.sendAndReceiveExistsAndNotEmptyTableMessage sendMessage:{}", sendMessage);
            receiveMessage = sendAndReceive(existsAndEmptyTableTopic, replyExistsAndNotEmptyTableTopic, sendMessage);
            log.info("OrderKafkaProducer.sendAndReceiveExistsAndNotEmptyTableMessage receiveMessage:{}", receiveMessage);
            return readOrderTableMessage(order, receiveMessage, sendMessage);
        } catch (JsonProcessingException | ExecutionException | TimeoutException e) {
            throw new OrderTableMessageStreamException(sendMessage, receiveMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderTableMessageStreamException(sendMessage, receiveMessage);
        }
    }

    private OrderTableMessage readOrderTableMessage(Order order, String receiveMessage, String sendMessage)
            throws JsonProcessingException {
        OrderTableMessage orderTableMessage = objectMapper.readValue(receiveMessage, OrderTableMessage.class);
        if (!Objects.equals(orderTableMessage.getOrderTableId(), order.getOrderTableId())) {
            throw new OrderTableMessageStreamException(sendMessage, receiveMessage);
        }
        return orderTableMessage;
    }

    private String sendAndReceive(String topic, String replyTopic, String message)
            throws ExecutionException, InterruptedException, TimeoutException {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, message);
        producerRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes()));
        RequestReplyFuture<String, String, String> requestReplyFuture =
                replyingKafkaTemplate.sendAndReceive(producerRecord);
        ConsumerRecord<String, String> consumerRecord = requestReplyFuture.get(10, TimeUnit.SECONDS);
        return consumerRecord.value();
    }
}
