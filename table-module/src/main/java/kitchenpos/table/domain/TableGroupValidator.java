package kitchenpos.table.domain;

import kitchenpos.table.exception.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class TableGroupValidator {
    public static final int MIN_ORDER_TABLES = 2;

    private final OrderTableValidator orderTableValidator;

    public TableGroupValidator(OrderTableValidator orderTableValidator) {
        this.orderTableValidator = orderTableValidator;
    }

    public boolean create(TableGroup tableGroup) {
        validateIfLessOrderTables(tableGroup.getOrderTables());
        validateOrderTables(tableGroup.getOrderTables());
        return true;
    }

    public boolean validateIfLessOrderTables(List<?> values) {
        if (isLessOrderTables(values)) {
            throw new RequiredOrderTablesOfTableGroupException();
        }
        return true;
    }

    private boolean isLessOrderTables(List<?> values) {
        return CollectionUtils.isEmpty(values) || values.size() < MIN_ORDER_TABLES;
    }

    public boolean validateIfNotFoundOrderTables(List<Long> orderTableIds, List<OrderTable> orderTables) {
        if (orderTableIds.size() != orderTables.size()) {
            throw new OrderTableNotFoundException();
        }
        return true;
    }

    private void validateOrderTables(List<OrderTable> orderTables) {
        orderTables.forEach(orderTableValidator::changeTableGroup);
    }
}
