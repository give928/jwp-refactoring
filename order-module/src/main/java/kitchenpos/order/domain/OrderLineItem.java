package kitchenpos.order.domain;

import javax.persistence.*;
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

    private OrderLineItem(Long seq, Order order, Long menuId, long quantity) {
        this.seq = seq;
        this.order = order;
        this.orderMenu = OrderMenu.from(menuId);
        this.quantity = quantity;
    }

    public static OrderLineItem of(Long menuId, long quantity) {
        return of(null, null, menuId, quantity);
    }

    public static OrderLineItem of(Long seq, Order order, Long menuId, long quantity) {
        return new OrderLineItem(seq, order, menuId, quantity);
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

    public long getQuantity() {
        return quantity;
    }

    public void initOrderMenu(OrderMenu orderMenu) {
        this.orderMenu = orderMenu;
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
