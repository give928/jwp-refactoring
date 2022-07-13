package kitchenpos.table.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static kitchenpos.table.TableFixtures.aOrderTable1;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTableChangedEmptyEventTest {
    @DisplayName("주문 테이블 빈 테이블 여부 변경 이벤트 도메인을 생성한다.")
    @Test
    void create() {
        // given
        OrderTable orderTable = aOrderTable1();

        // when
        OrderTableChangedEmptyEvent event = OrderTableChangedEmptyEvent.from(orderTable);

        // then
        assertThat(event.getOrderTableId()).isEqualTo(orderTable.getId());
        assertThat(event.getTableGroupId()).isEqualTo(Optional.ofNullable(orderTable.getTableGroup()).orElse(new TableGroup()).getId());
        assertThat(event.getNumberOfGuests()).isEqualTo(orderTable.getNumberOfGuests());
        assertThat(event.isEmpty()).isEqualTo(orderTable.isEmpty());
    }
}
