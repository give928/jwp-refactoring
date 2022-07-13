package kitchenpos.order.domain;

import java.util.Objects;

public class OrderTableChangedEmptyReceivedMessage {
    private Long orderTableId;
    private boolean empty;

    public OrderTableChangedEmptyReceivedMessage() {
    }

    private OrderTableChangedEmptyReceivedMessage(Long orderTableId, boolean empty) {
        this.orderTableId = orderTableId;
        this.empty = empty;
    }

    public static OrderTableChangedEmptyReceivedMessage from(Long orderTableId, boolean empty) {
        return new OrderTableChangedEmptyReceivedMessage(orderTableId, empty);
    }

    public Long getOrderTableId() {
        return orderTableId;
    }

    public boolean isEmpty() {
        return empty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderTableChangedEmptyReceivedMessage that = (OrderTableChangedEmptyReceivedMessage) o;
        return isEmpty() == that.isEmpty() && Objects.equals(getOrderTableId(), that.getOrderTableId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderTableId(), isEmpty());
    }
}
