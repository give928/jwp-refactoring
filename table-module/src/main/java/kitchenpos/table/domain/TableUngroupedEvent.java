package kitchenpos.table.domain;

import java.util.List;
import java.util.stream.Collectors;

public class TableUngroupedEvent {
    private final Long tableGroupId;
    private final List<Long> orderTableIds;

    public TableUngroupedEvent(TableGroup tableGroup) {
        this.tableGroupId = tableGroup.getId();
        this.orderTableIds = tableGroup.getOrderTables()
                .stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());
    }

    public static TableUngroupedEvent from(TableGroup tableGroup) {
        return new TableUngroupedEvent(tableGroup);
    }

    public Long getTableGroupId() {
        return tableGroupId;
    }

    public List<Long> getOrderTableIds() {
        return orderTableIds;
    }
}
