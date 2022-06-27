package kitchenpos.table.domain;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TableGroupValidatorTest {
    @Mock
    private OrderTableValidator orderTableValidator;

    @InjectMocks
    private TableGroupValidator tableGroupValidator;

    @DisplayName("주문 테이블의 단체 지정 유효성을 확인한다.")
    @Test
    void create() {
        // given
        OrderTables orderTables = aOrderTables1();

        // when
        boolean valid = tableGroupValidator.create(orderTables);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("주문 테이블은 2개 이상만 단체로 지정할 수 있다.")
    @Test
    void cannotCreateIfEmptyOrZeroOrderTable() {
        // given
        OrderTables orderTables = OrderTables.of(Collections.singletonList(aOrderTable1()),
                                                 new TableGroupValidator(orderTableValidator) {
                                                     @Override
                                                     public boolean create(OrderTables orderTables) {
                                                         return true;
                                                     }
                                                 });

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupValidator.create(orderTables);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 테이블 여부를 변경한다.")
    @Test
    void changeEmpty() {
        // given
        OrderTable orderTable = aTableGroup1().getOrderTables().get(0);

        given(orderTableValidator.changeEmpty(orderTable)).willReturn(true);

        // when
        boolean valid = tableGroupValidator.changeEmpty(orderTable);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("단체 지정된 주문 테이블은 빈 테이블 여부를 변경할 수 없다.")
    @Test
    void cannotChangeEmptyNotNullTableGroup() {
        // given
        OrderTable orderTable = aTableGroup1().getOrderTables().get(0);

        given(orderTableValidator.changeEmpty(orderTable)).willThrow(IllegalArgumentException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupValidator.changeEmpty(orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 빈 테이블 여부를 변경할 수 없다.")
    @Test
    void cannotChangeEmptyOrdersInCookingOrMeal() {
        // given
        OrderTable orderTable = aTableGroup1().getOrderTables().get(0);

        given(orderTableValidator.changeEmpty(orderTable)).willThrow(IllegalArgumentException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupValidator.changeEmpty(orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
}
