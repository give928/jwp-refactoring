package kitchenpos.table.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static kitchenpos.table.TableFixtures.aOrderTables;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTablesTest {
    @DisplayName("주문 테이블 일급 컬렉션을 생성한다.")
    @Test
    void create() {
        // given
        List<OrderTable> orderTableList = aOrderTables();

        // when
        OrderTables orderTables = OrderTables.of(orderTableList);

        // then
        assertThat(orderTables).isEqualTo(OrderTables.of(orderTableList));
    }

    @DisplayName("단체 지정을 한다.")
    @Test
    void changeTableGroup() {
        // given
        OrderTables orderTables = OrderTables.of(aOrderTables());
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
        OrderTables orderTables = OrderTables.of(aOrderTables());
        TableGroup tableGroup = new TableGroup();
        orderTables.group(tableGroup);

        // when
        orderTables.ungroup();

        // then
        assertThat(orderTables.get()).isEmpty();
    }
}
