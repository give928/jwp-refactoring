package kitchenpos.table.domain;

import org.springframework.util.CollectionUtils;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Embeddable
public class OrderTables {
    public static final int MIN_ORDER_TABLES = 2;

    @OneToMany(mappedBy = "tableGroup", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<OrderTable> values;

    protected OrderTables() {
    }

    private OrderTables(List<OrderTable> values) {
        validateOrderTables(values);
        this.values = values;
    }

    public static OrderTables from(List<OrderTable> values) {
        return new OrderTables(values);
    }

    private void validateOrderTables(List<OrderTable> values) {
        if (isLessOrderTables(values)) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isLessOrderTables(List<OrderTable> values) {
        return CollectionUtils.isEmpty(values) || values.size() < MIN_ORDER_TABLES;
    }

    public OrderTables changeTableGroup(TableGroup tableGroup) {
        values.forEach(orderTable -> orderTable.changeTableGroup(tableGroup));
        return this;
    }

    public void ungroup() {
        values.forEach(OrderTable::clearTableGroup);
    }

    public List<OrderTable> get() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderTables that = (OrderTables) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
