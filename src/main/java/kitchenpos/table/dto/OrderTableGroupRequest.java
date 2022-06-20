package kitchenpos.table.dto;

import kitchenpos.table.domain.OrderTable;

public class OrderTableGroupRequest {
    private Long id;

    public OrderTableGroupRequest() {
    }

    public OrderTableGroupRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public OrderTable toOrderTable() {
        return new OrderTable(id);
    }
}
