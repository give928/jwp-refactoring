package kitchenpos.order.domain;

import kitchenpos.order.exception.OrderMenusNotFoundException;
import kitchenpos.order.exception.OrderTableEmptyException;
import kitchenpos.order.exception.OrderTableNotFoundException;
import kitchenpos.order.exception.RequiredOrderLineItemException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static kitchenpos.order.OrderFixtures.*;
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
        OrderValidator orderValidator = new OrderValidator(aOrderEventPublisher()) {
            @Override
            public boolean create(Order order) {
                throw new RequiredOrderLineItemException();
            }
        };

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Order.of(1L, aOrderTable1(),
                                                                           orderLineItems, orderValidator, aOrderEventPublisher());

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredOrderLineItemException.class);
    }

    @DisplayName("주문 항목들은 등록된 메뉴만 가능하다.")
    @Test
    void cannotCreateIfNotExistsMenu() {
        // given
        Long menuId = -1L;
        List<OrderLineItem> orderLineItems = Collections.singletonList(OrderLineItem.of(menuId, 1));
        OrderEventPublisher orderEventPublisher = new OrderEventPublisher() {
            @Override
            public List<OrderMenuMessage> sendAndReceiveMenusMessage(List<OrderLineItem> orderLineItems) {
                return Collections.emptyList();
            }

            @Override
            public OrderTableMessage sendAndReceiveExistsAndNotEmptyTableMessage(Order order) {
                return new OrderTableMessage(order.getId(), true, false);
            }
        };

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Order.of(1L, aOrderTable1(),
                                                                           orderLineItems, aOrderValidator(), orderEventPublisher);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderMenusNotFoundException.class)
                .hasMessageContaining(OrderMenusNotFoundException.MESSAGE);
    }

    @DisplayName("등록된 주문 테이블만 주문을 할 수 있다.")
    @Test
    void cannotCreateIfNotExistsOrderTable() {
        // given
        Long orderTableId = -1L;
        OrderValidator orderValidator = new OrderValidator(aOrderEventPublisher()) {
            @Override
            public boolean create(Order order) {
                throw new OrderTableNotFoundException();
            }
        };

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Order.of(1L, orderTableId,
                                                                           aOrderLineItems1().get(), orderValidator, aOrderEventPublisher());

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableNotFoundException.class)
                .hasMessageContaining(OrderTableNotFoundException.MESSAGE);
    }

    @DisplayName("빈 테이블은 주문을 할 수 없다.")
    @Test
    void cannotCreateIfEmptyOrderTable() {
        // given
        OrderValidator orderValidator = new OrderValidator(aOrderEventPublisher()) {
            @Override
            public boolean create(Order order) {
                throw new OrderTableEmptyException();
            }
        };

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Order.of(1L, aOrderTable1(),
                                                                           aOrderLineItems1().get(), orderValidator, aOrderEventPublisher());

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableEmptyException.class)
                .hasMessageContaining(OrderTableEmptyException.MESSAGE);
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
