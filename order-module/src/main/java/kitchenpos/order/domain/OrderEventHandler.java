package kitchenpos.order.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);

    private final MessageBroadcaster messageBroadcaster;
    private final OrderValidator orderValidator;

    public OrderEventHandler(MessageBroadcaster messageBroadcaster, OrderValidator orderValidator) {
        this.messageBroadcaster = messageBroadcaster;
        this.orderValidator = orderValidator;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(OrderCreatedEvent event) {
        log.info("handle OrderCreatedEvent order:{}", event);
        OrderCreatedEventReceivedMessage eventReceivedMessage = messageBroadcaster.broadcast(event);
        orderValidator.created(eventReceivedMessage);
    }
}
