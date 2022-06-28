package kitchenpos.table.domain;

import kitchenpos.table.exception.OrderTableEmptyException;

public interface TableValidator {
    default boolean changeTableGroup(OrderTable orderTable) {
        validateIfEmpty(orderTable, Boolean.FALSE);
        return true;
    }

    default void validateIfEmpty(OrderTable orderTable, boolean empty) {
        if (orderTable.isEmpty() == empty) {
            throw OrderTableEmptyException.throwBy(empty);
        }
    }

    boolean changeEmpty(OrderTable orderTable);
}
