package kitchenpos.order.domain;

import java.util.List;
import java.util.Objects;

public class TableUngroupedReceivedMessage {
    private Long tableGroupId;
    private List<Long> orderTableIds;

    public TableUngroupedReceivedMessage() {
    }

    private TableUngroupedReceivedMessage(Long tableGroupId, List<Long> orderTableIds) {
        this.tableGroupId = tableGroupId;
        this.orderTableIds = orderTableIds;
    }

    public static TableUngroupedReceivedMessage from(Long tableGroupId, List<Long> orderTableIds) {
        return new TableUngroupedReceivedMessage(tableGroupId, orderTableIds);
    }

    public Long getTableGroupId() {
        return tableGroupId;
    }

    public List<Long> getOrderTableIds() {
        return orderTableIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableUngroupedReceivedMessage that = (TableUngroupedReceivedMessage) o;
        return Objects.equals(getTableGroupId(), that.getTableGroupId()) && Objects.equals(
                getOrderTableIds(), that.getOrderTableIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTableGroupId(), getOrderTableIds());
    }
}
