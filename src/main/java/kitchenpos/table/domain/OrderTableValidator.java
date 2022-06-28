package kitchenpos.table.domain;

import kitchenpos.table.exception.GroupedOrderTableException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrderTableValidator implements TableValidator {
    @Override
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
}
