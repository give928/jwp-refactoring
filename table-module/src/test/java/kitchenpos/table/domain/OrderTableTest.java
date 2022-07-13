package kitchenpos.table.domain;

import kitchenpos.table.TableFixtures;
import kitchenpos.table.exception.GroupedOrderTableException;
import kitchenpos.table.exception.OrderTableEmptyException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static kitchenpos.table.TableFixtures.*;
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
        orderTable.group(tableGroup);

        // then
        assertThat(orderTable.isEmpty()).isFalse();
        assertThat(orderTable.getTableGroup()).isNotNull();
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
        OrderTableValidator orderTableValidator = new OrderTableValidator();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.changeEmpty(orderTableValidator, true);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(GroupedOrderTableException.class)
                .hasMessageContaining(GroupedOrderTableException.MESSAGE);
    }

    /*@DisplayName("주문 상태가 조리중이거나 식사인 경우에는 빈 테이블 여부를 변경할 수 없다.")
    @Test
    void cannotChangeEmptyOrdersInCookingOrMeal() {
        // given
        OrderTable orderTable = TableFixtures.aOrderTable().empty(false).build();
        OrderTableValidator orderTableValidator = new OrderTableValidator();
        MessageBroadcaster messageBroadcaster = new MessageBroadcaster() {
            @Override
            public boolean sendOrderTableEmptyChangeMessage(OrderTable orderTable) {
                return false;
            }

            @Override
            public boolean sendGroupTableMessage(TableGroup tableGroup) {
                return true;
            }
        };

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.changeEmpty(orderTableValidator,
                                                                                         messageBroadcaster, true);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderNotCompletionException.class)
                .hasMessageContaining(OrderNotCompletionException.MESSAGE);
    }*/

    @DisplayName("방문 손님 수를 변경한다.")
    @Test
    void changeNumberOfGuests() {
        // given
        OrderTable orderTable = TableFixtures.aOrderTable().empty(false).build();

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
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.changeNumberOfGuests(orderTableValidator,
                                                                                                  1);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableEmptyException.class)
                .hasMessageContaining(OrderTableEmptyException.EMPTY_MESSAGE);
    }
}
