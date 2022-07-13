package kitchenpos.table.domain;

public class OrderTableChangedEmptyEvent {
    private final OrderTable orderTable;

    public OrderTableChangedEmptyEvent(OrderTable orderTable) {
        this.orderTable = orderTable;
    }

    public static OrderTableChangedEmptyEvent from(OrderTable orderTable) {
        return new OrderTableChangedEmptyEvent(orderTable);
    }

    public Long getOrderTableId() {
        return orderTable.getId();
    }

    public Long getTableGroupId() {
        if (orderTable.getTableGroup() != null) {
            return orderTable.getTableGroup().getId();
        }
        return null;
    }

    public int getNumberOfGuests() {
        return orderTable.getNumberOfGuests();
    }

    public boolean isEmpty() {
        return orderTable.isEmpty();
    }
}
