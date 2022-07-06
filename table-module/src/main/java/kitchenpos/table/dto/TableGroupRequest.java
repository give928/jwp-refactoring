package kitchenpos.table.dto;

import java.util.List;

public class TableGroupRequest {
    private List<OrderTableGroupRequest> orderTables;

    public TableGroupRequest() {
    }

    public TableGroupRequest(List<OrderTableGroupRequest> orderTables) {
        this.orderTables = orderTables;
    }

    public List<OrderTableGroupRequest> getOrderTables() {
        return orderTables;
    }
}
