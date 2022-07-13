package kitchenpos.order;

import kitchenpos.order.domain.*;
import kitchenpos.order.domain.Order.OrderBuilder;
import kitchenpos.order.dto.MenuResponse;

import java.time.LocalDateTime;
import java.util.Arrays;

public class OrderFixtures {
    public static OrderMenu aOrderMenu1() {
        return OrderMenu.of(1L, "음식1", 1_000L);
    }

    public static OrderMenu aOrderMenu2() {
        return OrderMenu.of(2L, "음식2", 2_000L);
    }

    public static OrderMenu aOrderMenu3() {
        return OrderMenu.of(3L, "음식3", 3_000L);
    }

    public static Long aOrderTable1() {
        return 1L;
    }

    public static Long aOrderTable2() {
        return 2L;
    }

    public static OrderLineItem aOrderLineItem1() {
        return OrderLineItem.of(1L, null, aOrderMenu1(), 1);
    }

    public static OrderLineItem aOrderLineItem2() {
        return OrderLineItem.of(2L, null, aOrderMenu2(), 1);
    }

    public static OrderLineItem aOrderLineItem3() {
        return OrderLineItem.of(3L, null, aOrderMenu3(), 1);
    }

    public static OrderLineItems aOrderLineItems1() {
        return OrderLineItems.of(Arrays.asList(aOrderLineItem1(), aOrderLineItem2()));
    }

    public static OrderLineItems aOrderLineItems2() {
        return OrderLineItems.of(Arrays.asList(aOrderLineItem2(), aOrderLineItem3()));
    }

    public static OrderBuilder aOrder1() {
        return Order.builder()
                .id(1L)
                .orderTableId(aOrderTable1())
                .orderStatus(OrderStatus.COOKING)
                .orderedTime(LocalDateTime.now())
                .orderLineItems(aOrderLineItems1().get());
    }

    public static OrderBuilder aOrder2() {
        return Order.builder()
                .id(2L)
                .orderTableId(aOrderTable2())
                .orderStatus(OrderStatus.COOKING)
                .orderedTime(LocalDateTime.now())
                .orderLineItems(aOrderLineItems2().get());
    }

    public static OrderValidator aOrderValidator() {
        return new OrderValidator() {
            @Override
            public boolean place(Order order) {
                return true;
            }
        };
    }
}
