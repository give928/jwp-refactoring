package kitchenpos.table.domain;

public interface TableEventPublisher {
    boolean sendOrderTableEmptyChangeMessage(OrderTable orderTable);

    boolean sendGroupTableMessage(TableGroup tableGroup);
}
