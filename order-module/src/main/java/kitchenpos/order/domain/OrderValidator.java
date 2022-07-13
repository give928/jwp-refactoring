package kitchenpos.order.domain;

import kitchenpos.order.exception.OrderCompletionException;
import kitchenpos.order.exception.OrderTableEmptyException;
import kitchenpos.order.exception.OrderTableNotFoundException;
import kitchenpos.order.exception.RequiredOrderLineItemException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Objects;

@Component
public class OrderValidator {
    public boolean place(Order order) {
        validateOrderLineItems(order);
        return true;
    }

    private void validateOrderLineItems(Order order) {
        if (CollectionUtils.isEmpty(order.getOrderLineItems())) {
            throw new RequiredOrderLineItemException();
        }
    }

    public boolean created(OrderCreatedEventReceivedMessage eventReceivedMessage) {
        validateIfInvalidOrderTable(eventReceivedMessage);
        return true;
    }

    private void validateIfInvalidOrderTable(OrderCreatedEventReceivedMessage eventReceivedMessage) {
        if (!eventReceivedMessage.isExists()) {
            throw new OrderTableNotFoundException();
        }
        if (eventReceivedMessage.isEmpty()) {
            throw new OrderTableEmptyException();
        }
    }

    public boolean changeOrderStatus(Order order) {
        if (Objects.equals(OrderStatus.COMPLETION, order.getOrderStatus())) {
            throw new OrderCompletionException();
        }
        return true;
    }
}
