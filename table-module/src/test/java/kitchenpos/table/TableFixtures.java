package kitchenpos.table;

import kitchenpos.table.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableFixtures {

    public static OrderTable.OrderTableBuilder aOrderTable() {
        return OrderTable.builder()
                .id(1L)
                .tableGroup(null)
                .numberOfGuests(0)
                .empty(true);
    }

    public static OrderTable aOrderTable1() {
        return OrderTable.of(1L, null, 0, true);
    }

    public static OrderTable aOrderTable2() {
        return OrderTable.of(2L, null, 0, true);
    }

    public static List<OrderTable> aOrderTables() {
        return new ArrayList<>(Arrays.asList(aOrderTable1(), aOrderTable2()));
    }

    public static TableGroup aTableGroup1() {
        return TableGroup.of(1L, aOrderTables(), aTableGroupValidator());
    }

    public static OrderTableValidator aOrderTableValidator() {
        return new OrderTableValidator() {
            @Override
            public boolean changeEmpty(OrderTable orderTable) {
                return true;
            }
        };
    }

    public static TableGroupValidator aTableGroupValidator() {
        return new TableGroupValidator(aOrderTableValidator());
    }
}
