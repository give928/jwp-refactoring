package kitchenpos.order.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Embeddable
public class OrderLineItems {
    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<OrderLineItem> values;

    protected OrderLineItems() {
    }

    private OrderLineItems(List<OrderLineItem> values) {
        this.values = values;
    }

    public static OrderLineItems of(List<OrderLineItem> values) {
        return new OrderLineItems(values);
    }

    public OrderLineItems initOrder(Order order) {
        values.forEach(orderLineItem -> orderLineItem.initOrder(order));
        return this;
    }

    public List<OrderLineItem> get() {
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
        OrderLineItems that = (OrderLineItems) o;
        return Objects.equals(get(), that.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get());
    }
}
