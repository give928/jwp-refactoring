package kitchenpos.table.domain;

import kitchenpos.table.exception.GroupedOrderTableException;
import kitchenpos.table.exception.OrderNotCompletionException;
import kitchenpos.table.exception.OrderTableEmptyException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrderTableValidator {
    private final TableEventPublisher tableEventPublisher;

    public OrderTableValidator(TableEventPublisher tableEventPublisher) {
        this.tableEventPublisher = tableEventPublisher;
    }

    public boolean changeEmpty(OrderTable orderTable) {
        validateIfNotNullTableGroup(orderTable);
        validateIfOrderStatusNotCompletion(orderTable);
        return true;
    }

    private void validateIfNotNullTableGroup(OrderTable orderTable) {
        if (Objects.nonNull(orderTable.getTableGroup())) {
            throw new GroupedOrderTableException();
        }
    }

    private void validateIfOrderStatusNotCompletion(OrderTable orderTable) {
        boolean orderStatusCompletion = tableEventPublisher.sendOrderTableEmptyChangeMessage(orderTable);
        if (!orderStatusCompletion) {
            throw new OrderNotCompletionException();
        }
    }

    public boolean changeNumberOfGuests(OrderTable orderTable) {
        validateIfEmpty(orderTable, Boolean.TRUE);
        return true;
    }

    private void validateIfEmpty(OrderTable orderTable, boolean empty) {
        if (orderTable.isEmpty() == empty) {
            throw OrderTableEmptyException.throwBy(empty);
        }
    }
}
