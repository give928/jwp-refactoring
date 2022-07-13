package kitchenpos.order.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class OrderLineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_line_item_orders"))
    private Order order;

    @Embedded
    private OrderMenu orderMenu;

    private long quantity;

    public OrderLineItem() {
    }

    private OrderLineItem(Long seq, Order order, OrderMenu orderMenu, long quantity) {
        this.seq = seq;
        this.order = order;
        this.orderMenu = orderMenu;
        this.quantity = quantity;
    }

    public static OrderLineItem of(OrderMenu orderMenu, long quantity) {
        return of(null, null, orderMenu, quantity);
    }

    public static OrderLineItem of(Long seq, Order order, OrderMenu orderMenu, long quantity) {
        return new OrderLineItem(seq, order, orderMenu, quantity);
    }

    public Long getSeq() {
        return seq;
    }

    public Order getOrder() {
        return order;
    }

    public Long getMenuId() {
        return orderMenu.getMenuId();
    }

    public String getMenuName() {
        return orderMenu.getMenuName();
    }

    public BigDecimal getMenuPrice() {
        return orderMenu.getMenuPrice();
    }

    public long getQuantity() {
        return quantity;
    }

    public void initOrder(Order order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderLineItem that = (OrderLineItem) o;
        return Objects.equals(getSeq(), that.getSeq());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSeq());
    }
}
