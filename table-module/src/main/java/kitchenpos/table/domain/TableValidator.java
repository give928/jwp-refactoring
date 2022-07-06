package kitchenpos.table.domain;

import kitchenpos.table.exception.GroupedOrderTableException;
import kitchenpos.table.exception.OrderTableEmptyException;

public interface TableValidator {
    default boolean changeTableGroup(OrderTable orderTable) {
        validateIfNotNullTableGroup(orderTable);
        validateIfEmpty(orderTable, Boolean.FALSE);
        return true;
    }

    private void validateIfNotNullTableGroup(OrderTable orderTable) {
        if (orderTable.getTableGroup() != null) {
            throw new GroupedOrderTableException();
        }
    }

    default void validateIfEmpty(OrderTable orderTable, boolean empty) {
        if (orderTable.isEmpty() == empty) {
            throw OrderTableEmptyException.throwBy(empty);
        }
    }

    boolean changeEmpty(OrderTable orderTable);
}
