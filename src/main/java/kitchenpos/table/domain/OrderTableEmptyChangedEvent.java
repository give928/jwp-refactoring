package kitchenpos.table.domain;

public class OrderTableEmptyChangedEvent {
    private final Long id;

    public OrderTableEmptyChangedEvent(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
