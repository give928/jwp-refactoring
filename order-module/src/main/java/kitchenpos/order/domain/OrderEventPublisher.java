package kitchenpos.order.domain;

import java.util.List;

public interface OrderEventPublisher {
    List<OrderMenuMessage> sendAndReceiveMenusMessage(List<OrderLineItem> orderLineItems);

    OrderTableMessage sendAndReceiveExistsAndNotEmptyTableMessage(Order order);
}
