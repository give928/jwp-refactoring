package kitchenpos.order.domain;

public interface OrderEventPublisher {
    OrderTableMessage sendAndReceiveExistsAndNotEmptyTableMessage(Order order);

    boolean sendAndReceiveExistsMenusMessage(Order order);
}
