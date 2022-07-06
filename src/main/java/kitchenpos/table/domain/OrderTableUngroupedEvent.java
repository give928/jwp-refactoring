package kitchenpos.table.domain;

import java.util.List;

public class OrderTableUngroupedEvent {
    private final List<Long> ids;

    public OrderTableUngroupedEvent(List<Long> ids) {
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }
}
