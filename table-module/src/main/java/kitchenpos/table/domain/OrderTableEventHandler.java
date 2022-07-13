package kitchenpos.table.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderTableEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderTableEventHandler.class);

    private final MessageBroadcaster messageBroadcaster;

    public OrderTableEventHandler(MessageBroadcaster messageBroadcaster) {
        this.messageBroadcaster = messageBroadcaster;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderTableChangedEmptyEvent event) {
        log.info("handle orderTableId:{}, tableGroupId:{}, numberOfGuests:{}, empty:{}", event.getOrderTableId(), event.getTableGroupId(), event.getNumberOfGuests(), event.isEmpty());
        messageBroadcaster.broadcast(event);
    }
}
