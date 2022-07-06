package kitchenpos.table.domain;

import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderValidator;
import kitchenpos.order.exception.OrderNotCompletionException;
import kitchenpos.table.exception.GroupedOrderTableException;
import kitchenpos.table.exception.OrderTableEmptyException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTableTest {
    private OrderTableValidator orderTableValidator;

    @BeforeEach
    void setUp() {
        orderTableValidator = aOrderTableValidator();
    }

    @DisplayName("주문 테이블을 생성한다.")
    @Test
    void create() {
        // when
        OrderTable orderTable = aOrderTable1();

        // then
        assertThat(orderTable).isEqualTo(OrderTable.of(1L, null, 1, true));
    }

    @DisplayName("단체 지정을 한다.")
    @Test
    void changeTableGroup() {
        // given
        OrderTable orderTable = aOrderTable1();
        TableGroup tableGroup = new TableGroup();

        // when
        orderTable.group(orderTableValidator, tableGroup);

        // then
        assertThat(orderTable.isEmpty()).isFalse();
        assertThat(orderTable.getTableGroup()).isNotNull();
    }

    @DisplayName("비어있지 않은 테이블은 단체 지정을 할 수 없다.")
    @Test
    void cannotChangeTableGroupIfNotEmpty() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 1, false);
        TableGroup tableGroup = new TableGroup();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.group(orderTableValidator, tableGroup);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableEmptyException.class)
                .hasMessageContaining(OrderTableEmptyException.NOT_EMPTY_MESSAGE);
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void clearTableGroup() {
        // given
        OrderTable orderTable = aTableGroup1().getOrderTables().get(0);

        // when
        orderTable.ungroup();

        // then
        assertThat(orderTable.getTableGroup()).isNull();
    }

    @DisplayName("빈 테이블 여부를 변경한다.")
    @Test
    void changeEmpty() {
        // given
        OrderTable orderTable = aOrderTable1();

        // when
        orderTable.changeEmpty(orderTableValidator, false);

        // then
        assertThat(orderTable.isEmpty()).isFalse();
    }

    @DisplayName("단체 지정된 주문 테이블은 빈 테이블 여부를 변경할 수 없다.")
    @Test
    void cannotChangeEmptyNotNullTableGroup() {
        // given
        OrderTable orderTable = aTableGroup1().getOrderTables().get(0);
        TableValidator tableValidator = new OrderTableValidator() {
            @Override
            public boolean changeEmpty(OrderTable orderTable) {
                throw new GroupedOrderTableException();
            }
        };

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.changeEmpty(tableValidator, true);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(GroupedOrderTableException.class)
                .hasMessageContaining(GroupedOrderTableException.MESSAGE);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 빈 테이블 여부를 변경할 수 없다.")
    @Test
    void cannotChangeEmptyOrdersInCookingOrMeal() {
        // given
        OrderTable orderTable = aOrderTable().empty(false).build();
        TableValidator tableValidator = new OrderTableValidator() {
            @Override
            public boolean changeEmpty(OrderTable orderTable) {
                throw new OrderNotCompletionException();
            }
        };

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.changeEmpty(tableValidator, true);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderNotCompletionException.class)
                .hasMessageContaining(OrderNotCompletionException.MESSAGE);
    }

    @DisplayName("방문 손님 수를 변경한다.")
    @Test
    void changeNumberOfGuests() {
        // given
        OrderTable orderTable = aOrderTable().empty(false).build();

        // when
        orderTable.changeNumberOfGuests(orderTableValidator, 1);

        // then
        assertThat(orderTable.getNumberOfGuests()).isEqualTo(1);
    }

    @DisplayName("빈 테이블은 방문 손님 수를 변경할 수 없다.")
    @Test
    void cannotChangeNumberOfGuestsIfEmpty() {
        // given
        OrderTable orderTable = aOrderTable1();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.changeNumberOfGuests(orderTableValidator, 1);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableEmptyException.class)
                .hasMessageContaining(OrderTableEmptyException.EMPTY_MESSAGE);
    }
}
