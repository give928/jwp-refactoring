package kitchenpos.table.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Embeddable
public class OrderTables {
    @OneToMany(mappedBy = "tableGroup", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<OrderTable> values;

    protected OrderTables() {
    }

    private OrderTables(List<OrderTable> values) {
        this.values = values;
    }

    public static OrderTables of(List<OrderTable> values) {
        return new OrderTables(values);
    }

    public OrderTables group(TableGroup tableGroup) {
        values.forEach(orderTable -> orderTable.group(tableGroup));
        return this;
    }

    public void ungroup() {
        values.forEach(OrderTable::ungroup);
    }

    public List<OrderTable> get() {
        return Collections.unmodifiableList(values);
    }

    public List<Long> getIds() {
        return values.stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());
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