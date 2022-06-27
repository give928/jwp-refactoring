package kitchenpos.table.domain;

public interface TableValidator {
    default boolean changeTableGroup(OrderTable orderTable) {
        validateIfEmpty(orderTable, Boolean.FALSE);
        return true;
    }

    default void validateIfEmpty(OrderTable orderTable, boolean empty) {
        if (orderTable.isEmpty() == empty) {
            throw new IllegalArgumentException();
        }
    }

    boolean changeEmpty(OrderTable orderTable);
}
