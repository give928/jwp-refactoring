package kitchenpos.order.domain;

import kitchenpos.order.exception.OrderCompletionException;
import kitchenpos.order.exception.OrderTableEmptyException;
import kitchenpos.order.exception.OrderTableNotFoundException;
import kitchenpos.order.exception.RequiredOrderLineItemException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static kitchenpos.order.OrderFixtures.aOrder1;
import static kitchenpos.order.OrderFixtures.aOrderValidator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class OrderValidatorTest {
    @InjectMocks
    private OrderValidator orderValidator;

    @DisplayName("주문 유효성을 확인한다.")
    @Test
    void place() {
        // given
        Order order = aOrder1().build();

        // when
        boolean valid = orderValidator.place(order);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("주문 항목이 1개 미만이면 주문 유효성 확인이 실패한다.")
    @Test
    void cannotPlaceIfEmptyOrderLineItem() {
        // given
        List<OrderLineItem> orderLineItems = Collections.emptyList();
        Order order = aOrder1().orderLineItems(orderLineItems).build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.place(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredOrderLineItemException.class)
                .hasMessageContaining(RequiredOrderLineItemException.MESSAGE);
    }

    @DisplayName("주문 생성 이벤트 유효성을 확인한다.")
    @Test
    void created() {
        // given
        OrderCreatedEventReceivedMessage orderCreatedEventReceivedMessage =
                new OrderCreatedEventReceivedMessage(1L, 1L, true, false, null, null);

        // when
        boolean valid = orderValidator.created(orderCreatedEventReceivedMessage);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("주문 테이블이 없으면 주문 생성 이벤트 유효성 확인이 실패한다.")
    @Test
    void cannotCreatedIfOrderTableNotFound() {
        // given
        OrderCreatedEventReceivedMessage orderCreatedEventReceivedMessage =
                new OrderCreatedEventReceivedMessage(1L, 1L, false, false, null, null);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.created(orderCreatedEventReceivedMessage);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableNotFoundException.class)
                .hasMessageContaining(OrderTableNotFoundException.MESSAGE);
    }

    @DisplayName("주문 테이블이 비어있으면 주문 생성 이벤트 유효성 확인이 실패한다.")
    @Test
    void cannotCreatedIfEmptyOrderTable() {
        // given
        OrderCreatedEventReceivedMessage orderCreatedEventReceivedMessage =
                new OrderCreatedEventReceivedMessage(1L, 1L, true, true, null, null);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.created(orderCreatedEventReceivedMessage);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableEmptyException.class)
                .hasMessageContaining(OrderTableEmptyException.MESSAGE);
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
