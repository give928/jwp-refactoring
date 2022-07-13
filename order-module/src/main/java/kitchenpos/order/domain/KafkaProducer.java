package kitchenpos.order.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.common.exception.KafkaMessageSendException;
import kitchenpos.order.exception.OrderCreatedEventMessageBroadcastException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaProducer implements MessageBroadcaster {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    @Value("${kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topics.reply-order-created}")
    private String replyOrderCreatedTopic;

    @Value("${kafka.topics.order-uncompleted}")
    private String orderUncompletedTopic;

    @Value("${kafka.topics.orders-uncompleted}")
    private String ordersUncompletedTopic;

    private final ObjectMapper objectMapper;

    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(ObjectMapper objectMapper,
                         ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate,
                         KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public OrderCreatedEventReceivedMessage broadcast(OrderCreatedEvent event) {
        String sendMessage = null;
        String receiveMessage = null;
        try {
            sendMessage = objectMapper.writeValueAsString(event);
            receiveMessage = sendAndReceive(orderCreatedTopic, replyOrderCreatedTopic, sendMessage);
            log.info("broadcast topic:{}, replyTopic:{}, receiveMessage:{}", orderCreatedTopic, replyOrderCreatedTopic, receiveMessage);
            OrderCreatedEventReceivedMessage eventReceivedMessage =
                    objectMapper.readValue(receiveMessage, OrderCreatedEventReceivedMessage.class)
                            .init(sendMessage, receiveMessage);
            return validateReceivedMessage(event, eventReceivedMessage);
        } catch (JsonProcessingException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            throw new OrderCreatedEventMessageBroadcastException(sendMessage, receiveMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderCreatedEventMessageBroadcastException(sendMessage, receiveMessage);
        }
    }

    private OrderCreatedEventReceivedMessage validateReceivedMessage(OrderCreatedEvent event,
                                                                     OrderCreatedEventReceivedMessage eventReceivedMessage) {
        if (!Objects.equals(event.getOrderId(), eventReceivedMessage.getOrderId())) {
            throw new OrderCreatedEventMessageBroadcastException(eventReceivedMessage);
        }
        return eventReceivedMessage;
    }

    private String sendAndReceive(String topic, String replyTopic, String sendMessage)
            throws ExecutionException, InterruptedException, TimeoutException {
        log.info("sendAndReceive topic:{}, replyTopic:{}, sendMessage:{}", topic, replyTopic, sendMessage);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, sendMessage);
        producerRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes()));
        RequestReplyFuture<String, String, String> requestReplyFuture =
                replyingKafkaTemplate.sendAndReceive(producerRecord);
        ConsumerRecord<String, String> consumerRecord = requestReplyFuture.get(10, TimeUnit.SECONDS);
        return consumerRecord.value();
    }

    @Override
    public ListenableFuture<SendResult<String, String>> broadcast(OrderUncompletedSendingMessage payload) {
        return send(orderUncompletedTopic, payload);
    }

    @Override
    public ListenableFuture<SendResult<String, String>> broadcast(OrdersUncompletedSendingMessage payload) {
        return send(ordersUncompletedTopic, payload);
    }

    private <T> ListenableFuture<SendResult<String, String>> send(String topic, T payload) {
        String sendMessage = null;
        try {
            sendMessage = objectMapper.writeValueAsString(payload);
            log.info("send topic:{}, sendMessage:{}", topic, sendMessage);
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, sendMessage);
            return addDefaultCallback(future, topic, sendMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new KafkaMessageSendException(sendMessage, e);
        }
    }

    private ListenableFuture<SendResult<String, String>> addDefaultCallback(ListenableFuture<SendResult<String, String>> future, String topic, String sendMessage) {
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, String> message) {
                log.info("succeed topic:{}, sent message:{}, offset:{} ", topic, message, message.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                log.error("fail topic:{}, send message:{}", topic, sendMessage, throwable);
                throw new KafkaMessageSendException(sendMessage, throwable);
            }
        });
        return future;
    }
}
