package kitchenpos.order.domain;

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_table_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_order_table"))
    private Long orderTableId;

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

    private Order(Long id, Long orderTableId, OrderStatus orderStatus, LocalDateTime orderedTime,
                  List<OrderLineItem> orderLineItems, OrderValidator orderValidator) {
        this.id = id;
        this.orderTableId = orderTableId;
        this.orderStatus = orderStatus;
        this.orderedTime = orderedTime;
        this.orderLineItems = OrderLineItems.from(Optional.ofNullable(orderLineItems)
                                                          .orElse(new ArrayList<>()))
                .initOrder(this);
        orderValidator.create(this);
    }

    public static Order of(Long orderTableId, List<OrderLineItem> orderLineItems, OrderValidator orderValidator) {
        return of(null, orderTableId, OrderStatus.COOKING, LocalDateTime.now(), orderLineItems, orderValidator);
    }

    public static Order of(Long id, Long orderTableId, List<OrderLineItem> orderLineItems, OrderValidator orderValidator) {
        return of(id, orderTableId, OrderStatus.COOKING, LocalDateTime.now(), orderLineItems, orderValidator);
    }

    public static Order of(Long id, Long orderTableId, OrderStatus orderStatus, LocalDateTime orderedTime,
                           List<OrderLineItem> orderLineItems, OrderValidator orderValidator) {
        return new Order(id, orderTableId, orderStatus, orderedTime, orderLineItems, orderValidator);
    }

    public Order changeOrderStatus(OrderValidator orderValidator, OrderStatus orderStatus) {
        orderValidator.changeOrderStatus(this);
        this.orderStatus = orderStatus;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderTableId() {
        return orderTableId;
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
