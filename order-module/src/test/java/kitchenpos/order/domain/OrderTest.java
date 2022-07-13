package kitchenpos.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static kitchenpos.order.OrderFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {
    @DisplayName("주문을 생성한다.")
    @Test
    void create() {
        // when
        Order order = aOrder1().build();

        // then
        assertThat(order.getId()).isEqualTo(1L);
    }

    @DisplayName("주문 처리를 한다.")
    @Test
    void place() {
        // given
        Order order = aOrder1().orderStatus(null).build();

        // when
        Order placeOrder = order.place(aOrderValidator());

        // then
        assertThat(placeOrder.getOrderStatus()).isEqualTo(OrderStatus.COOKING);
    }

    @DisplayName("주문 상태를 변경한다.")
    @Test
    void changeOrderStatus() {
        // given
        Order order = aOrder1().build();
        OrderStatus meal = OrderStatus.MEAL;
        OrderValidator orderValidator = aOrderValidator();

        // when
        Order changedOrder = order.changeOrderStatus(orderValidator, meal);

        // then
        assertThat(changedOrder.getOrderStatus()).isEqualTo(meal);
    }
}
