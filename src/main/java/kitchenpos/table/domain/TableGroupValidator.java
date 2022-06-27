package kitchenpos.table.domain;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class TableGroupValidator implements TableValidator {
    public static final int MIN_ORDER_TABLES = 2;

    private final OrderTableValidator orderTableValidator;

    public TableGroupValidator(OrderTableValidator orderTableValidator) {
        this.orderTableValidator = orderTableValidator;
    }

    public boolean create(OrderTables orderTables) {
        validateOrderTables(orderTables.get());
        return true;
    }

    private void validateOrderTables(List<OrderTable> values) {
        if (isLessOrderTables(values)) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isLessOrderTables(List<OrderTable> values) {
        return CollectionUtils.isEmpty(values) || values.size() < MIN_ORDER_TABLES;
    }

    @Override
    public boolean changeEmpty(OrderTable orderTable) {
        return orderTableValidator.changeEmpty(orderTable);
    }
}
