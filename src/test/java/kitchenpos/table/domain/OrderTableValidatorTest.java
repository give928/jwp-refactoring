package kitchenpos.table.domain;

import kitchenpos.order.domain.OrderRepository;
import kitchenpos.order.domain.OrderTableEventHandler;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OrderTableValidatorTest {
    @Mock
    private OrderTableEventHandler orderTableEventHandler;

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

    @DisplayName("주문 테이블의 빈 테이블 변경 유효성을 확인한다.")
    @Test
    void changeEmpty() {
        // given
        OrderTable orderTable = aOrderTable1();

        // when
        boolean valid = orderTableValidator.changeEmpty(orderTable);

        // then
        assertThat(valid).isTrue();
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
