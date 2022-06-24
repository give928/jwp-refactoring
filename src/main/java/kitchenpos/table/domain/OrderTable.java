package kitchenpos.table.domain;

import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.Orders;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
public class OrderTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_group_id", foreignKey = @ForeignKey(name = "fk_order_table_table_group"))
    private TableGroup tableGroup;

    @Embedded
    private NumberOfGuests numberOfGuests;

    private boolean empty;

    @Embedded
    private Orders orders;

    protected OrderTable() {
    }

    private OrderTable(Long id, TableGroup tableGroup, int numberOfGuests, boolean empty, List<Order> orders) {
        this.id = id;
        this.tableGroup = tableGroup;
        this.numberOfGuests = NumberOfGuests.from(numberOfGuests);
        this.empty = empty;
        this.orders = Orders.from(Optional.ofNullable(orders)
                                          .orElse(new ArrayList<>()));
    }

    public static OrderTable of(int numberOfGuests, boolean empty) {
        return of(null, null, numberOfGuests, empty);
    }

    public static OrderTable of(Long id, TableGroup tableGroup, int numberOfGuests, boolean empty) {
        return of(id, tableGroup, numberOfGuests, empty, new ArrayList<>());
    }

    public static OrderTable of(Long id, TableGroup tableGroup, int numberOfGuests, boolean empty, List<Order> orders) {
        return new OrderTable(id, tableGroup, numberOfGuests, empty, orders);
    }

    public Long getId() {
        return id;
    }

    public TableGroup getTableGroup() {
        return tableGroup;
    }

    public int getNumberOfGuests() {
        return numberOfGuests.get();
    }

    public boolean isEmpty() {
        return empty;
    }

    public void clearTableGroup() {
        validateIfOrdersInCookingOrMeal();
        this.tableGroup = null;
    }

    public void changeTableGroup(TableGroup tableGroup) {
        validateIfEmpty(Boolean.FALSE);
        changeEmpty(Boolean.FALSE);
        this.tableGroup = tableGroup;
    }

    public OrderTable changeNumberOfGuests(int numberOfGuests) {
        validateIfEmpty(Boolean.TRUE);
        this.numberOfGuests = NumberOfGuests.from(numberOfGuests);
        return this;
    }

    public OrderTable changeEmpty(boolean empty) {
        validateChangeEmpty();
        this.empty = empty;
        return this;
    }

    public void addOrder(Order order) {
        this.orders.add(order);
        order.initOrderTable(this);
    }

    private void validateIfEmpty(boolean empty) {
        if (isEmpty() == empty) {
            throw new IllegalArgumentException();
        }
    }

    private void validateChangeEmpty() {
        validateIfNotNullTableGroup();
        validateIfOrdersInCookingOrMeal();
    }

    private void validateIfOrdersInCookingOrMeal() {
        if (orders.hasCookingOrMeal()) {
            throw new IllegalArgumentException();
        }
    }

    private void validateIfNotNullTableGroup() {
        if (Objects.nonNull(getTableGroup())) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderTable that = (OrderTable) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
