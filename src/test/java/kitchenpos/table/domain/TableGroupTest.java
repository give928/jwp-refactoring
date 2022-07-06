package kitchenpos.table.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TableGroupTest {
    @DisplayName("단체 지정을 생성한다.")
    @Test
    void create() {
        // given
        OrderTable orderTable1 = OrderTable.of(1L, null, 0, true);
        OrderTable orderTable2 = OrderTable.of(2L, null, 0, true);
        List<OrderTable> orderTables = Arrays.asList(orderTable1, orderTable2);

        // when
        TableGroup tableGroup = TableGroup.of(1L, orderTables);

        // then
        assertThat(tableGroup.getOrderTables()).containsExactly(orderTable1, orderTable2);
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void ungroup() {
        // given
        OrderTable orderTable1 = OrderTable.of(1L, null, 0, true);
        OrderTable orderTable2 = OrderTable.of(2L, null, 0, true);
        List<OrderTable> orderTables = Arrays.asList(orderTable1, orderTable2);
        TableGroup tableGroup = TableGroup.of(1L, orderTables);

        // when
        tableGroup.ungroup();

        // then
        assertThat(orderTables).extracting("tableGroup")
                .containsExactly(null, null);
    }
}
