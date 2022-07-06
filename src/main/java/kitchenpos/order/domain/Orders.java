package kitchenpos.order.domain;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Embeddable
public class Orders {
    @OneToMany(mappedBy = "orderTable", orphanRemoval = true)
    private List<Order> values;

    protected Orders() {
    }

    private Orders(List<Order> values) {
        this.values = values;
    }

    public static Orders from(List<Order> values) {
        return new Orders(values);
    }

    public void add(Order order) {
        values.add(order);
    }

    public boolean hasCookingOrMeal() {
        return values.stream()
                .anyMatch(Order::isCookingOrMeal);
    }

    public List<Order> get() {
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
        Orders orders = (Orders) o;
        return Objects.equals(get(), orders.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get());
    }
}
