package kitchenpos.order.domain;

import java.util.Objects;
import java.util.stream.Collectors;

public class OrderCreatedEvent {
    private final Order order;

    public OrderCreatedEvent(Order order) {
        this.order = order;
    }

    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(order);
    }

    public Long getOrderId() {
        return order.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderCreatedEvent event = (OrderCreatedEvent) o;
        return Objects.equals(order, event.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order);
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "orderId=" + order.getId() +
                ",orderTableId=" + order.getOrderTableId() +
                ",orderStatus=" + order.getOrderStatus() +
                ",orderedTime=" + order.getOrderedTime() +
                ",menuIds=" + order.getOrderLineItems().stream().map(OrderLineItem::getMenuId).collect(Collectors.toList()) +
                '}';
    }
}
