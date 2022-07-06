package kitchenpos.order;

import kitchenpos.order.domain.OrderEventPublisher;
import kitchenpos.order.domain.*;
import kitchenpos.order.domain.Order.OrderBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class OrderFixtures {
    public static Long aMenu1() {
        return 1L;
    }

    public static Long aMenu2() {
        return 2L;
    }

    public static Long aMenu3() {
        return 3L;
    }

    public static Long aOrderTable1() {
        return 1L;
    }

    public static Long aOrderTable2() {
        return 2L;
    }

    public static OrderLineItem aOrderLineItem1() {
        return OrderLineItem.of(1L, null, aMenu1(), 1);
    }

    public static OrderLineItem aOrderLineItem2() {
        return OrderLineItem.of(2L, null, aMenu2(), 1);
    }

    public static OrderLineItem aOrderLineItem3() {
        return OrderLineItem.of(3L, null, aMenu3(), 1);
    }

    public static OrderLineItems aOrderLineItems1() {
        return OrderLineItems.from(Arrays.asList(aOrderLineItem1(), aOrderLineItem2()));
    }

    public static OrderLineItems aOrderLineItems2() {
        return OrderLineItems.from(Arrays.asList(aOrderLineItem2(), aOrderLineItem3()));
    }

    public static OrderBuilder aOrder1() {
        return Order.builder()
                .id(1L)
                .orderTableId(aOrderTable1())
                .orderStatus(OrderStatus.COOKING)
                .orderedTime(LocalDateTime.now())
                .orderLineItems(aOrderLineItems1().get())
                .orderValidator(aOrderValidator());
    }

    public static OrderBuilder aOrder2() {
        return Order.builder()
                .id(2L)
                .orderTableId(aOrderTable2())
                .orderStatus(OrderStatus.COOKING)
                .orderedTime(LocalDateTime.now())
                .orderLineItems(aOrderLineItems2().get())
                .orderValidator(aOrderValidator());
    }

    public static OrderValidator aOrderValidator() {
        return new OrderValidator(aOrderMessageStream()) {
            @Override
            public boolean create(Order order) {
                return true;
            }
        };
    }

    public static List<OrderStatus> aNotCompletionOrderStatuses() {
        return Arrays.asList(OrderStatus.COOKING, OrderStatus.MEAL);
    }

    public static OrderEventPublisher aOrderMessageStream() {
        return new OrderEventPublisher() {
            @Override
            public OrderTableMessage sendAndReceiveExistsAndNotEmptyTableMessage(Order order) {
                return new OrderTableMessage(order.getId(), true, false);
            }

            @Override
            public boolean sendAndReceiveExistsMenusMessage(Order order) {
                return true;
            }
        };
    }
}
