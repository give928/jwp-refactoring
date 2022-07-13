package kitchenpos.order.domain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OrderUncompletedSendingMessage {
    private final OrderTableChangedEmptyReceivedMessage payload;
    private final List<Order> uncompletedOrders;

    private OrderUncompletedSendingMessage(OrderTableChangedEmptyReceivedMessage payload, List<Order> uncompletedOrders) {
        this.payload = payload;
        this.uncompletedOrders = uncompletedOrders;
    }

    public static OrderUncompletedSendingMessage from(OrderTableChangedEmptyReceivedMessage payload, List<Order> uncompletedOrders) {
        return new OrderUncompletedSendingMessage(payload, uncompletedOrders);
    }

    public Long getOrderTableId() {
        return payload.getOrderTableId();
    }

    public boolean isEmpty() {
        return payload.isEmpty();
    }

    public List<Long> getUncompletedOrderIds() {
        return uncompletedOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderUncompletedSendingMessage that = (OrderUncompletedSendingMessage) o;
        return Objects.equals(payload, that.payload) && Objects.equals(uncompletedOrders,
                                                                       that.uncompletedOrders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload, uncompletedOrders);
    }
}
