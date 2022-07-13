package kitchenpos.table.domain;

import java.util.List;

public class OrderUncompletedReceivedMessage {
    private Long orderTableId;
    private boolean empty;
    private List<Long> uncompletedOrderIds;

    public Long getOrderTableId() {
        return orderTableId;
    }

    public boolean isEmpty() {
        return empty;
    }

    public List<Long> getUncompletedOrderIds() {
        return uncompletedOrderIds;
    }
}
