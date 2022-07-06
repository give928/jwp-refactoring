package kitchenpos.table.domain;

import kitchenpos.table.exception.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class TableGroupValidator {
    public static final int MIN_ORDER_TABLES = 2;

    private final TableEventPublisher tableEventPublisher;

    public TableGroupValidator(TableEventPublisher tableEventPublisher) {
        this.tableEventPublisher = tableEventPublisher;
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
        orderTables.forEach(this::changeTableGroup);
    }

    private void changeTableGroup(OrderTable orderTable) {
        validateIfNotNullTableGroup(orderTable);
        validateIfEmpty(orderTable, Boolean.FALSE);
    }

    private void validateIfNotNullTableGroup(OrderTable orderTable) {
        if (orderTable.getTableGroup() != null) {
            throw new GroupedOrderTableException();
        }
    }

    private void validateIfEmpty(OrderTable orderTable, boolean empty) {
        if (orderTable.isEmpty() == empty) {
            throw OrderTableEmptyException.throwBy(empty);
        }
    }

    public void ungroup(TableGroup tableGroup) {
        boolean orderStatusCompletion = tableEventPublisher.sendGroupTableMessage(tableGroup);
        if (!orderStatusCompletion) {
            throw new OrderNotCompletionException();
        }
    }
}
