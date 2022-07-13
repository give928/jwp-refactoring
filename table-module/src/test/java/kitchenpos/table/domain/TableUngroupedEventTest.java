package kitchenpos.table.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static kitchenpos.table.TableFixtures.aTableGroup1;
import static org.assertj.core.api.Assertions.assertThat;

class TableUngroupedEventTest {
    @DisplayName("단체 지정 해제 이벤트 도메인을 생성한다.")
    @Test
    void create() {
        // given
        TableGroup tableGroup = aTableGroup1();

        // when
        TableUngroupedEvent event = TableUngroupedEvent.from(tableGroup);

        // then
        assertThat(event.getTableGroupId()).isEqualTo(tableGroup.getId());
        assertThat(event.getOrderTableIds()).isEqualTo(tableGroup.getOrderTables()
                                                               .stream()
                                                               .map(OrderTable::getId)
                                                               .collect(Collectors.toList()));
    }
}
