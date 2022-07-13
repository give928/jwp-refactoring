package kitchenpos.table.domain;

import java.util.List;

public class OrdersUncompletedReceivedMessage {
    private Long tableGroupId;
    private List<Long> orderTableIds;
    private List<Long> uncompletedOrderIds;

    public Long getTableGroupId() {
        return tableGroupId;
    }

    public List<Long> getOrderTableIds() {
        return orderTableIds;
    }

    public List<Long> getUncompletedOrderIds() {
        return uncompletedOrderIds;
    }
}
