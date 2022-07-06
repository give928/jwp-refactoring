package kitchenpos.table.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static kitchenpos.table.TableFixtures.aOrderTable1;
import static kitchenpos.table.TableFixtures.aOrderTable2;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTablesTest {
    private OrderTable orderTable1;
    private OrderTable orderTable2;

    @BeforeEach
    void setUp() {
        orderTable1 = aOrderTable1();
        orderTable2 = aOrderTable2();
    }

    @DisplayName("주문 테이블 일급 컬렉션을 생성한다.")
    @Test
    void create() {
        // given
        List<OrderTable> orderTableList = Arrays.asList(orderTable1, orderTable2);

        // when
        OrderTables orderTables = OrderTables.of(orderTableList);

        // then
        assertThat(orderTables).isEqualTo(OrderTables.of(orderTableList));
    }

    @DisplayName("단체 지정을 한다.")
    @Test
    void changeTableGroup() {
        // given
        OrderTables orderTables = OrderTables.of(Arrays.asList(orderTable1, orderTable2));
        TableGroup tableGroup = new TableGroup();

        // when
        OrderTables changedOrderTables = orderTables.group(tableGroup);

        // then
        assertThat(changedOrderTables.get()).extracting("tableGroup")
                .containsExactly(tableGroup, tableGroup);
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void ungroup() {
        // given
        OrderTables orderTables = OrderTables.of(Arrays.asList(orderTable1, orderTable2));
        TableGroup tableGroup = new TableGroup();
        orderTables.group(tableGroup);

        // when
        orderTables.ungroup();

        // then
        assertThat(orderTables.get()).extracting("tableGroup")
                .containsExactly(null, null);
    }
}
