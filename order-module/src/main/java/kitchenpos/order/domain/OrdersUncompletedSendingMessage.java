package kitchenpos.order.domain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OrdersUncompletedSendingMessage {
    private final TableUngroupedReceivedMessage payload;
    private final List<Order> uncompletedOrders;

    private OrdersUncompletedSendingMessage(TableUngroupedReceivedMessage payload, List<Order> uncompletedOrders) {
        this.payload = payload;
        this.uncompletedOrders = uncompletedOrders;
    }

    public static OrdersUncompletedSendingMessage from(TableUngroupedReceivedMessage payload, List<Order> uncompletedOrders) {
        return new OrdersUncompletedSendingMessage(payload, uncompletedOrders);
    }

    public Long getTableGroupId() {
        return payload.getTableGroupId();
    }

    public List<Long> getOrderTableIds() {
        return payload.getOrderTableIds();
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
        OrdersUncompletedSendingMessage that = (OrdersUncompletedSendingMessage) o;
        return Objects.equals(payload, that.payload) && Objects.equals(uncompletedOrders,
                                                                       that.uncompletedOrders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload, uncompletedOrders);
    }
}
