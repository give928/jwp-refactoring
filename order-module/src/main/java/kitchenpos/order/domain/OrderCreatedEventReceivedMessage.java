package kitchenpos.order.domain;

import java.util.Objects;

public class OrderCreatedEventReceivedMessage {
    private Long orderId;
    private Long orderTableId;
    private boolean exists;
    private boolean empty;
    private String sentMessage;
    private String receivedMessage;

    public OrderCreatedEventReceivedMessage() {
    }

    public OrderCreatedEventReceivedMessage(Long orderId, Long orderTableId, boolean exists, boolean empty,
                                            String sentMessage, String receivedMessage) {
        this.orderId = orderId;
        this.orderTableId = orderTableId;
        this.exists = exists;
        this.empty = empty;
        this.sentMessage = sentMessage;
        this.receivedMessage = receivedMessage;
    }

    public Long getOrderId() {
        return orderId;
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

    public String getSentMessage() {
        return sentMessage;
    }

    public String getReceivedMessage() {
        return receivedMessage;
    }

    public OrderCreatedEventReceivedMessage init(String sentMessage, String receivedMessage) {
        this.sentMessage = sentMessage;
        this.receivedMessage = receivedMessage;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderCreatedEventReceivedMessage that = (OrderCreatedEventReceivedMessage) o;
        return isExists() == that.isExists() && isEmpty() == that.isEmpty() && Objects.equals(getOrderId(),
                                                                                              that.getOrderId()) && Objects.equals(
                getOrderTableId(), that.getOrderTableId()) && Objects.equals(getSentMessage(),
                                                                             that.getSentMessage()) && Objects.equals(
                getReceivedMessage(), that.getReceivedMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderId(), getOrderTableId(), isExists(), isEmpty(), getSentMessage(),
                            getReceivedMessage());
    }
}
