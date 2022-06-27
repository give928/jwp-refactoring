package kitchenpos.order.domain;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {
    @DisplayName("주문을 생성한다.")
    @Test
    void create() {
        // when
        Order order = aOrder1().build();

        // then
        assertThat(order.getId()).isEqualTo(1L);
    }

    @DisplayName("주문 항목을 1개 이상 입력해야 한다.")
    @Test
    void cannotCreateIfEmptyOrderLineItem() {
        // given
        List<OrderLineItem> orderLineItems = Collections.emptyList();
        OrderValidator orderValidator = aOrderValidatorThrownByCreate();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Order.of(1L, aOrderTable1().getId(),
                                                                           orderLineItems, orderValidator);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 항목들은 등록된 메뉴만 가능하다.")
    @Test
    void cannotCreateIfNotExistsMenu() {
        // given
        Long menuId = -1L;
        List<OrderLineItem> orderLineItems = Collections.singletonList(OrderLineItem.of(menuId, 1));
        OrderValidator orderValidator = aOrderValidatorThrownByCreate();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Order.of(1L, aOrderTable1().getId(),
                                                                           orderLineItems, orderValidator);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("등록된 주문 테이블만 주문을 할 수 있다.")
    @Test
    void cannotCreateIfNotExistsOrderTable() {
        // given
        Long orderTableId = -1L;
        OrderValidator orderValidator = aOrderValidatorThrownByCreate();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Order.of(1L, orderTableId,
                                                                           aOrderLineItems1().get(), orderValidator);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 테이블은 주문을 할 수 없다.")
    @Test
    void cannotCreateIfEmptyOrderTable() {
        // given
        OrderValidator orderValidator = aOrderValidatorThrownByCreate();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Order.of(1L, aOrderTable1().getId(),
                                                                           aOrderLineItems1().get(), orderValidator);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
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
