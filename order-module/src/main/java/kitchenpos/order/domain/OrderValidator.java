package kitchenpos.order.domain;

import kitchenpos.order.exception.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Objects;

@Component
@Transactional(readOnly = true)
public class OrderValidator {
    private final OrderEventPublisher orderEventPublisher;

    public OrderValidator(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    public boolean create(Order order) {
        validateOrderLineItems(order);
        validateOrderTable(order);
        return true;
    }

    private void validateOrderTable(Order order) {
        OrderTableMessage orderTableMessage = orderEventPublisher.sendAndReceiveExistsAndNotEmptyTableMessage(order);
        if (!orderTableMessage.isExists()) {
            throw new OrderTableNotFoundException();
        }
        if (orderTableMessage.isEmpty()) {
            throw new OrderTableEmptyException();
        }
    }

    private void validateOrderLineItems(Order order) {
        if (CollectionUtils.isEmpty(order.getOrderLineItems())) {
            throw new RequiredOrderLineItemException();
        }
        boolean valid = orderEventPublisher.sendAndReceiveExistsMenusMessage(order);
        if (!valid) {
            throw new OrderMenusNotFoundException();
        }
    }

    public boolean changeOrderStatus(Order order) {
        if (Objects.equals(OrderStatus.COMPLETION, order.getOrderStatus())) {
            throw new OrderCompletionException();
        }
        return true;
    }
}
