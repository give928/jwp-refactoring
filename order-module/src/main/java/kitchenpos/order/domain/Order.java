package kitchenpos.order.domain;

import kitchenpos.order.exception.OrderMenusNotFoundException;
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

    @Column(nullable = false)
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
                  List<OrderLineItem> orderLineItems, OrderValidator orderValidator,
                  OrderEventPublisher orderEventPublisher) {
        this.id = id;
        this.orderTableId = orderTableId;
        this.orderStatus = orderStatus;
        this.orderedTime = orderedTime;
        this.orderLineItems = initOrderLineItems(orderLineItems, orderEventPublisher);
        orderValidator.create(this);
    }

    private OrderLineItems initOrderLineItems(List<OrderLineItem> orderLineItems,
                                              OrderEventPublisher orderEventPublisher) {
        List<OrderMenuMessage> orderMenuMessages = orderEventPublisher.sendAndReceiveMenusMessage(orderLineItems);
        orderLineItems.forEach(orderLineItem -> orderLineItem.initOrderMenu(
                orderMenuMessages.stream()
                        .filter(orderMenuMessage -> Objects.equals(orderMenuMessage.getId(), orderLineItem.getMenuId()))
                        .findAny()
                        .map(OrderMenuMessage::toOrderMenu)
                        .orElseThrow(OrderMenusNotFoundException::new)));
        return OrderLineItems.of(orderLineItems)
                .initOrder(this);
    }

    public static Order of(Long orderTableId, List<OrderLineItem> orderLineItems, OrderValidator orderValidator,
                           OrderEventPublisher orderEventPublisher) {
        return of(null, orderTableId, OrderStatus.COOKING, LocalDateTime.now(), orderLineItems, orderValidator,
                  orderEventPublisher);
    }

    public static Order of(Long id, Long orderTableId, List<OrderLineItem> orderLineItems,
                           OrderValidator orderValidator, OrderEventPublisher orderEventPublisher) {
        return of(id, orderTableId, OrderStatus.COOKING, LocalDateTime.now(), orderLineItems, orderValidator,
                  orderEventPublisher);
    }

    public static Order of(Long id, Long orderTableId, OrderStatus orderStatus, LocalDateTime orderedTime,
                           List<OrderLineItem> orderLineItems, OrderValidator orderValidator,
                           OrderEventPublisher orderEventPublisher) {
        return new Order(id, orderTableId, orderStatus, orderedTime, orderLineItems, orderValidator,
                         orderEventPublisher);
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

    public static Order.OrderBuilder builder() {
        return new Order.OrderBuilder();
    }

    public static class OrderBuilder {
        private Long id;
        private Long orderTableId;
        private OrderStatus orderStatus;
        private LocalDateTime orderedTime;
        private List<OrderLineItem> orderLineItems;
        private OrderValidator orderValidator;
        private OrderEventPublisher orderEventPublisher;

        OrderBuilder() {
        }

        public Order.OrderBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public Order.OrderBuilder orderTableId(Long orderTableId) {
            this.orderTableId = orderTableId;
            return this;
        }

        public Order.OrderBuilder orderStatus(OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
            return this;
        }

        public Order.OrderBuilder orderedTime(LocalDateTime orderedTime) {
            this.orderedTime = orderedTime;
            return this;
        }

        public Order.OrderBuilder orderLineItems(List<OrderLineItem> orderLineItems) {
            this.orderLineItems = orderLineItems;
            return this;
        }

        public Order.OrderBuilder orderValidator(OrderValidator orderValidator) {
            this.orderValidator = orderValidator;
            return this;
        }

        public Order.OrderBuilder orderEventPublisher(OrderEventPublisher orderEventPublisher) {
            this.orderEventPublisher = orderEventPublisher;
            return this;
        }

        public Order build() {
            return new Order(this.id, this.orderTableId, this.orderStatus, this.orderedTime, this.orderLineItems,
                             this.orderValidator, this.orderEventPublisher);
        }
    }
}
