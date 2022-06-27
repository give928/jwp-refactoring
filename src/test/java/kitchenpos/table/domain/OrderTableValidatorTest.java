package kitchenpos.table.domain;

import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderRepository;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OrderTableValidatorTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderTableValidator orderTableValidator;

    @DisplayName("주문 테이블의 단체 지정 유효성을 확인한다.")
    @Test
    void changeTableGroup() {
        // given
        OrderTable orderTable = aOrderTable1();

        // when
        boolean valid = orderTableValidator.changeTableGroup(orderTable);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("비어있지 않은 테이블은 단체 지정 유효성 확인이 실패한다.")
    @Test
    void cannotChangeTableGroupIfNotEmpty() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 0, false);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTableValidator.changeTableGroup(orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 테이블의 단체 지정 해제 유효성을 확인한다.")
    @Test
    void clearTableGroup() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 0, true);
        given(orderRepository.findByOrderTableId(orderTable.getId())).willReturn(Collections.emptyList());

        // when
        boolean valid = orderTableValidator.clearTableGroup(orderTable);

        // then
        assertThat(valid).isTrue();

        then(orderRepository).should(times(1)).findByOrderTableId(orderTable.getId());
    }

    @DisplayName("주문 테이블에 주문 상태가 조리중이거나 식사인 주문이 있으면 단체 지정 해제 유효성 확인이 실패한다.")
    @Test
    void cannotClearTableGroup() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 0, true);
        given(orderRepository.findByOrderTableId(orderTable.getId()))
                .willReturn(Collections.singletonList(
                        Order.of(1L, orderTable.getId(),
                                 Collections.singletonList(OrderLineItem.of(1L, null, 1L, 1)),
                                 aOrderValidator())));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTableValidator.clearTableGroup(orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 테이블의 빈 테이블 변경 유효성을 확인한다.")
    @Test
    void changeEmpty() {
        // given
        OrderTable orderTable = aOrderTable1();

        given(orderRepository.findByOrderTableId(orderTable.getId())).willReturn(Collections.emptyList());

        // when
        boolean valid = orderTableValidator.changeEmpty(orderTable);

        // then
        assertThat(valid).isTrue();

        then(orderRepository).should(times(1)).findByOrderTableId(orderTable.getId());
    }

    @DisplayName("주문 테이블이 단체 지정이 되어있으면 빈 테이블 변경 유효성 확인이 실패한다.")
    @Test
    void cannotChangeEmptyIfTableGroup() {
        // given
        OrderTable orderTable = aTableGroup1().getOrderTables().get(0);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTableValidator.changeEmpty(orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 테이블에 주문 상태가 조리중이거나 식사인 주문이 있으면 빈 테이블 변경 유효성 확인이 실패한다.")
    @Test
    void cannotChangeEmptyIfInvalidOrderStatus() {
        // given
        OrderTable orderTable = aOrderTable1();
        given(orderRepository.findByOrderTableId(orderTable.getId()))
                .willReturn(Collections.singletonList(
                        Order.of(1L, orderTable.getId(),
                                 Collections.singletonList(OrderLineItem.of(1L, null, 1L, 1)),
                                 aOrderValidator())));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTableValidator.changeEmpty(orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("방문 손님 수를 변경 유효성을 확인한다.")
    @Test
    void changeNumberOfGuests() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 0, false);

        // when
        boolean valid = orderTableValidator.changeNumberOfGuests(orderTable);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("빈 테이블은 방문 손님 수 변경 유효성 확인이 실패한다.")
    @Test
    void cannotChangeNumberOfGuestsIfEmpty() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 0, true);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTableValidator.changeNumberOfGuests(orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
}
