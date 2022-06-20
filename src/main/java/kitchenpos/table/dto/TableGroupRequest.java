package kitchenpos.table.dto;

import kitchenpos.table.domain.TableGroup;

import java.util.List;
import java.util.stream.Collectors;

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

    public TableGroup toTableGroup() {
        return new TableGroup(orderTables.stream()
                                      .map(OrderTableGroupRequest::toOrderTable)
                                      .collect(Collectors.toList()));
    }
}
