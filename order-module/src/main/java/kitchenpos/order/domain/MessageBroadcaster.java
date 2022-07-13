package kitchenpos.order.domain;

import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

public interface MessageBroadcaster {
    OrderCreatedEventReceivedMessage broadcast(OrderCreatedEvent event);

    ListenableFuture<SendResult<String, String>> broadcast(OrderUncompletedSendingMessage payload);

    ListenableFuture<SendResult<String, String>> broadcast(OrdersUncompletedSendingMessage payload);
}
