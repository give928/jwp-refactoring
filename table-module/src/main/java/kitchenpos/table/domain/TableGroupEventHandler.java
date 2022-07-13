package kitchenpos.table.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TableGroupEventHandler {
    private static final Logger log = LoggerFactory.getLogger(TableGroupEventHandler.class);

    private final MessageBroadcaster messageBroadcaster;

    public TableGroupEventHandler(MessageBroadcaster messageBroadcaster) {
        this.messageBroadcaster = messageBroadcaster;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TableUngroupedEvent event) {
        log.info("handle tableGroupId:{}, orderTableIds:{}", event.getTableGroupId(), event.getOrderTableIds());
        messageBroadcaster.broadcast(event);
    }
}
