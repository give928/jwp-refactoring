package kitchenpos.table;

import kitchenpos.table.domain.TableEventPublisher;
import kitchenpos.table.domain.*;

import java.util.Arrays;

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

    public static OrderTables aOrderTables1() {
        return OrderTables.of(Arrays.asList(aOrderTable1(), aOrderTable2()));
    }

    public static TableGroup aTableGroup1() {
        return TableGroup.of(1L, aOrderTables1().get(), aTableGroupValidator());
    }

    public static OrderTableValidator aOrderTableValidator() {
        return new OrderTableValidator(aTableEventPublisher()) {
            @Override
            public boolean changeEmpty(OrderTable orderTable) {
                return true;
            }
        };
    }

    public static TableGroupValidator aTableGroupValidator() {
        return new TableGroupValidator(aTableEventPublisher());
    }

    public static TableEventPublisher aTableEventPublisher() {
        return new TableEventPublisher() {
            @Override
            public boolean sendOrderTableEmptyChangeMessage(OrderTable orderTable) {
                return true;
            }

            @Override
            public boolean sendGroupTableMessage(TableGroup tableGroup) {
                return true;
            }
        };
    }
}
