package kitchenpos.table.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Embeddable
public class OrderTables {
    @OneToMany(mappedBy = "tableGroup", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<OrderTable> values;

    protected OrderTables() {
    }

    private OrderTables(List<OrderTable> values, TableGroupValidator tableGroupValidator) {
        this.values = values;
        tableGroupValidator.create(this);
    }

    public static OrderTables of(List<OrderTable> values, TableGroupValidator tableGroupValidator) {
        return new OrderTables(values, tableGroupValidator);
    }

    public OrderTables changeTableGroup(TableGroupValidator tableGroupValidator, TableGroup tableGroup) {
        values.forEach(orderTable -> orderTable.changeTableGroup(tableGroupValidator, tableGroup));
        return this;
    }

    public void ungroup(TableGroupValidator tableGroupValidator) {
        values.forEach(orderTable -> orderTable.clearTableGroup(tableGroupValidator));
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
