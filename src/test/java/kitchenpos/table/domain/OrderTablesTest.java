package kitchenpos.table.domain;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTablesTest {
    private OrderTable orderTable1;
    private OrderTable orderTable2;
    private TableGroupValidator tableGroupValidator;

    @BeforeEach
    void setUp() {
        orderTable1 = aOrderTable1();
        orderTable2 = aOrderTable2();
        tableGroupValidator = aTableGroupValidator();
    }

    @DisplayName("주문 테이블 일급 컬렉션을 생성한다.")
    @Test
    void create() {
        // given
        List<OrderTable> orderTableList = Arrays.asList(orderTable1, orderTable2);

        // when
        OrderTables orderTables = OrderTables.of(orderTableList, tableGroupValidator);

        // then
        assertThat(orderTables).isEqualTo(OrderTables.of(orderTableList, tableGroupValidator));
    }

    @DisplayName("2개 미만 주문 테이블은 생성할 수 없다.")
    @Test
    void cannotCreateLess() {
        // given
        List<OrderTable> orderTableList = Collections.singletonList(orderTable1);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> OrderTables.of(orderTableList, tableGroupValidator);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("단체 지정을 한다.")
    @Test
    void changeTableGroup() {
        // given
        OrderTables orderTables = OrderTables.of(Arrays.asList(orderTable1, orderTable2), tableGroupValidator);
        TableGroup tableGroup = new TableGroup();

        // when
        OrderTables changedOrderTables = orderTables.changeTableGroup(tableGroupValidator, tableGroup);

        // then
        assertThat(changedOrderTables.get()).extracting("tableGroup")
                .containsExactly(tableGroup, tableGroup);
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void ungroup() {
        // given
        OrderTables orderTables = OrderTables.of(Arrays.asList(orderTable1, orderTable2), tableGroupValidator);
        TableGroup tableGroup = new TableGroup();
        orderTables.changeTableGroup(tableGroupValidator, tableGroup);

        // when
        orderTables.ungroup(tableGroupValidator);

        // then
        assertThat(orderTables.get()).extracting("tableGroup")
                .containsExactly(null, null);
    }
}
