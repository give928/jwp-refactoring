package kitchenpos.order.domain;

import kitchenpos.table.domain.OrderTable;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_table_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_order_table"))
    private OrderTable orderTable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime orderedTime;

    @Embedded
    private OrderLineItems orderLineItems;

    protected Order() {
    }

    private Order(Long id, OrderTable orderTable, OrderStatus orderStatus, LocalDateTime orderedTime,
                  OrderLineItems orderLineItems) {
        validateOrderTable(orderTable);
        this.id = id;
        this.orderTable = orderTable;
        this.orderStatus = orderStatus;
        this.orderedTime = orderedTime;
        this.orderLineItems = orderLineItems.initOrder(this);
    }

    public static Order of(OrderTable orderTable, OrderLineItems orderLineItems) {
        return of(null, orderTable, OrderStatus.COOKING, LocalDateTime.now(), orderLineItems);
    }

    public static Order of(Long id, OrderTable orderTable, OrderLineItems orderLineItems) {
        return of(id, orderTable, OrderStatus.COOKING, LocalDateTime.now(), orderLineItems);
    }

    public static Order of(Long id, OrderTable orderTable, OrderStatus orderStatus, LocalDateTime orderedTime,
                           OrderLineItems orderLineItems) {
        return new Order(id, orderTable, orderStatus, orderedTime, orderLineItems);
    }

    private void validateOrderTable(OrderTable orderTable) {
        if (orderTable != null && orderTable.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    public void initOrderTable(OrderTable orderTable) {
        this.orderTable = orderTable;
    }

    public Order changeOrderStatus(OrderStatus orderStatus) {
        validateIfOrderStatusCompletion();
        this.orderStatus = orderStatus;
        return this;
    }

    private void validateIfOrderStatusCompletion() {
        if (Objects.equals(OrderStatus.COMPLETION, getOrderStatus())) {
            throw new IllegalArgumentException();
        }
    }

    public Long getId() {
        return id;
    }

    public OrderTable getOrderTable() {
        return orderTable;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public boolean isCookingOrMeal() {
        return getOrderStatus().isCookingOrMeal();
    }

    public LocalDateTime getOrderedTime() {
        return orderedTime;
    }

    public List<OrderLineItem> getOrderLineItems() {
        return orderLineItems.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Order order = (Order) o;
        return Objects.equals(getId(), order.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
