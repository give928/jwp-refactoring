package kitchenpos.order.domain;

import kitchenpos.order.exception.*;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static kitchenpos.order.OrderFixtures.aOrder1;
import static kitchenpos.order.OrderFixtures.aOrderValidator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderValidatorTest {
    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderValidator orderValidator;

    @DisplayName("주문 생성 유효성을 확인한다.")
    @Test
    void create() {
        // given
        Order order = aOrder1().build();

        given(orderEventPublisher.sendAndReceiveExistsMenusMessage(order)).willReturn(true);
        given(orderEventPublisher.sendAndReceiveExistsAndNotEmptyTableMessage(order)).willReturn(new OrderTableMessage(order.getId(), true, false));

        // when
        boolean valid = orderValidator.create(order);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("주문 항목이 1개 미만이면 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfEmptyOrderLineItem() {
        // given
        List<OrderLineItem> orderLineItems = Collections.emptyList();
        Order order = aOrder1().orderLineItems(orderLineItems).build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredOrderLineItemException.class)
                .hasMessageContaining(RequiredOrderLineItemException.MESSAGE);
    }

    @DisplayName("등록되지 않은 메뉴는 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfNotExistsMenu() {
        // given
        Long menuId = -1L;
        List<OrderLineItem> orderLineItems = Collections.singletonList(OrderLineItem.of(menuId, 1));
        Order order = aOrder1().orderLineItems(orderLineItems).build();

        given(orderEventPublisher.sendAndReceiveExistsMenusMessage(order)).willThrow(OrderMenusNotFoundException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderMenusNotFoundException.class);
    }

    @DisplayName("등록되지 않은 주문 테이블은 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfNotExistsOrderTable() {
        // given
        Long orderTableId = -1L;
        Order order = aOrder1().orderTableId(orderTableId).build();

        given(orderEventPublisher.sendAndReceiveExistsMenusMessage(order)).willReturn(true);
        given(orderEventPublisher.sendAndReceiveExistsAndNotEmptyTableMessage(order)).willThrow(OrderTableNotFoundException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableNotFoundException.class);
    }

    @DisplayName("빈 테이블은 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfEmptyOrderTable() {
        // given
        Order order = aOrder1().build();

        given(orderEventPublisher.sendAndReceiveExistsMenusMessage(order)).willReturn(true);
        given(orderEventPublisher.sendAndReceiveExistsAndNotEmptyTableMessage(order)).willThrow(OrderTableEmptyException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableEmptyException.class);
    }

    @DisplayName("주문 상태 변경 유효성을 확인한다.")
    @Test
    void changeOrderStatus() {
        // given
        Order order = aOrder1().build();

        // when
        boolean valid = orderValidator.changeOrderStatus(order);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("주문 상태가 완료인 경우 주문 상태 변경 유효성 확인이 실패한다.")
    @Test
    void cannotChangeOrderStatusIfCompletion() {
        // given
        Order order = aOrder1().build();
        order.changeOrderStatus(aOrderValidator(), OrderStatus.COMPLETION);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.changeOrderStatus(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderCompletionException.class)
                .hasMessageContaining(OrderCompletionException.MESSAGE);
    }
}
