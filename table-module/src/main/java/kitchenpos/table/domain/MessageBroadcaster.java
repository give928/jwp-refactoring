package kitchenpos.table.domain;

import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

public interface MessageBroadcaster {
    ListenableFuture<SendResult<String, String>> broadcast(OrderTableChangedEmptyEvent event);

    ListenableFuture<SendResult<String, String>> broadcast(TableUngroupedEvent event);
}
