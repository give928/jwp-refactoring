package kitchenpos.order.domain;

public class OrderTableMessage {
    private Long orderTableId;
    private boolean exists;
    private boolean empty;

    public OrderTableMessage() {
    }

    public OrderTableMessage(Long orderTableId, boolean exists, boolean empty) {
        this.orderTableId = orderTableId;
        this.exists = exists;
        this.empty = empty;
    }

    public Long getOrderTableId() {
        return orderTableId;
    }

    public boolean isExists() {
        return exists;
    }

    public boolean isEmpty() {
        return empty;
    }
}
