package kitchenpos.table.domain;

import kitchenpos.table.exception.GroupedOrderTableException;
import kitchenpos.table.exception.OrderTableEmptyException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrderTableValidator {
    public boolean changeEmpty(OrderTable orderTable) {
        validateIfNotNullTableGroup(orderTable);
        return true;
    }

    private void validateIfNotNullTableGroup(OrderTable orderTable) {
        if (Objects.nonNull(orderTable.getTableGroup())) {
            throw new GroupedOrderTableException();
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

    public void changeTableGroup(OrderTable orderTable) {
        validateIfNotNullTableGroup(orderTable);
        validateIfEmpty(orderTable, Boolean.FALSE);
    }
}
