package kitchenpos.table.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.common.exception.KafkaMessageSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class KafkaProducer implements MessageBroadcaster {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    @Value("${kafka.topics.order-table-changed-empty}")
    private String orderTableChangedEmptyTopic;

    @Value("${kafka.topics.table-ungrouped}")
    private String tableUngroupedTopic;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public ListenableFuture<SendResult<String, String>> broadcast(OrderTableChangedEmptyEvent event) {
        return send(orderTableChangedEmptyTopic, event);
    }

    @Override
    public ListenableFuture<SendResult<String, String>> broadcast(TableUngroupedEvent event) {
        return send(tableUngroupedTopic, event);
    }

    private <T> ListenableFuture<SendResult<String, String>> send(String topic, T event) {
        String sendMessage = null;
        try {
            sendMessage = objectMapper.writeValueAsString(event);
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
