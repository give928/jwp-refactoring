package kitchenpos.table.domain;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTablesTest {
    @DisplayName("주문 테이블 일급 컬렉션을 생성한다.")
    @Test
    void create() {
        // given
        OrderTable orderTable1 = OrderTable.of(1L, null, 0, true);
        OrderTable orderTable2 = OrderTable.of(2L, null, 0, true);

        // when
        OrderTables orderTables = OrderTables.from(Arrays.asList(orderTable1, orderTable2));

        // then
        assertThat(orderTables).isEqualTo(OrderTables.from(Arrays.asList(orderTable1, orderTable2)));
    }

    @DisplayName("2개 미만 주문 테이블은 생성할 수 없다.")
    @Test
    void cannotCreateLess() {
        // given
        OrderTable orderTable1 = OrderTable.of(1L, null, 0, true);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> OrderTables.from(Collections.singletonList(orderTable1));

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("단체 지정을 한다.")
    @Test
    void changeTableGroup() {
        // given
        OrderTable orderTable1 = OrderTable.of(1L, null, 0, true);
        OrderTable orderTable2 = OrderTable.of(2L, null, 0, true);
        OrderTables orderTables = OrderTables.from(Arrays.asList(orderTable1, orderTable2));
        TableGroup tableGroup = new TableGroup();

        // when
        OrderTables changedOrderTables = orderTables.changeTableGroup(tableGroup);

        // then
        assertThat(changedOrderTables.get()).extracting("tableGroup")
                .containsExactly(tableGroup, tableGroup);
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void ungroup() {
        // given
        OrderTable orderTable1 = OrderTable.of(1L, null, 0, true);
        OrderTable orderTable2 = OrderTable.of(2L, null, 0, true);
        OrderTables orderTables = OrderTables.from(Arrays.asList(orderTable1, orderTable2));
        TableGroup tableGroup = new TableGroup();
        orderTables.changeTableGroup(tableGroup);

        // when
        orderTables.ungroup();

        // then
        assertThat(orderTables.get()).extracting("tableGroup")
                .containsExactly(null, null);
    }
}
