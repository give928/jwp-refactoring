package kitchenpos.table.domain;

import kitchenpos.table.exception.OrderTableNotFoundException;
import kitchenpos.table.exception.RequiredOrderTablesOfTableGroupException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static kitchenpos.table.TableFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TableGroupValidatorTest {
    @Mock
    private OrderTableValidator orderTableValidator;

    @Mock
    private MessageBroadcaster messageBroadcaster;

    @InjectMocks
    private TableGroupValidator tableGroupValidator;

    @DisplayName("주문 테이블의 단체 지정 유효성을 확인한다.")
    @Test
    void create() {
        // given
        TableGroup tableGroup = new TableGroup(1L, OrderTables.of(Arrays.asList(aOrderTable1(), aOrderTable2())));

        // when
        boolean valid = tableGroupValidator.create(tableGroup);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("주문 테이블은 2개 미만이면 주문 테이블의 단체 지정 유효성 확인이 실패한다.")
    @Test
    void validateIfLessOrderTables1() {
        // given
        List<Long> orderTableIds = Collections.singletonList(1L);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupValidator.validateIfLessOrderTables(
                orderTableIds);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredOrderTablesOfTableGroupException.class)
                .hasMessageContaining(RequiredOrderTablesOfTableGroupException.MESSAGE);
    }

    @DisplayName("주문 테이블이 조회되지 않으면 주문 테이블의 단체 지정 유효성 확인이 실패한다.")
    @Test
    void validateIfLessOrderTables2() {
        // given
        List<Long> orderTableIds = Arrays.asList(1L, 2L);
        List<OrderTable> orderTables = Collections.singletonList(aOrderTable1());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupValidator.validateIfNotFoundOrderTables(
                orderTableIds, orderTables);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableNotFoundException.class)
                .hasMessageContaining(OrderTableNotFoundException.MESSAGE);
    }

    @DisplayName("주문 테이블은 2개 미만이면 주문 테이블의 단체 지정 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfEmptyOrZeroOrderTable() {
        // given
        TableGroup tableGroup = new TableGroup(1L, OrderTables.of(Collections.singletonList(aOrderTable1())));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupValidator.create(tableGroup);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredOrderTablesOfTableGroupException.class)
                .hasMessageContaining(RequiredOrderTablesOfTableGroupException.MESSAGE);
    }

    /*@DisplayName("빈 테이블 변경 유효성을 확인한다.")
    @Test
    void changeEmpty() {
        // given
        OrderTable orderTable = aTableGroup1().getOrderTables().get(0);

        given(orderTableValidator.validateIfNotNullTableGroup(orderTable)).willReturn(true);

        // when
        boolean valid = tableGroupValidator.changeEmpty(orderTable);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("단체 지정된 주문 테이블은 빈 테이블 변경 유효성을 확인이 실패한다.")
    @Test
    void cannotChangeEmptyNotNullTableGroup() {
        // given
        OrderTable orderTable = aTableGroup1().getOrderTables().get(0);

        given(orderTableValidator.validateIfNotNullTableGroup(orderTable)).willThrow(GroupedOrderTableException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupValidator.changeEmpty(orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(GroupedOrderTableException.class);
    }*/

    /*@DisplayName("주문 상태가 조리중이거나 식사인 경우에는 빈 테이블 변경 유효성을 확인이 실패한다.")
    @Test
    void cannotChangeEmptyOrdersInCookingOrMeal() {
        // given
        OrderTable orderTable = aTableGroup1().getOrderTables().get(0);

        given(orderTableValidator.changeEmpty(orderTable)).willThrow(OrderNotCompletionException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupValidator.changeEmpty(orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderNotCompletionException.class);
    }*/
}
