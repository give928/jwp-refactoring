package kitchenpos.table.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static kitchenpos.table.TableFixtures.aTableGroup1;
import static kitchenpos.table.TableFixtures.aTableGroupValidator;
import static org.assertj.core.api.Assertions.assertThat;

class TableGroupTest {
    @DisplayName("단체 지정을 생성한다.")
    @Test
    void create() {
        // when
        TableGroup tableGroup = aTableGroup1();

        // then
        assertThat(tableGroup.getOrderTables()).hasSize(tableGroup.getOrderTables().size())
                .extracting("tableGroup")
                .allMatch(Objects::nonNull);
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void ungroup() {
        // given
        TableGroup tableGroup = aTableGroup1();

        // when
        tableGroup.ungroup(aTableGroupValidator());

        // then
        assertThat(tableGroup.getOrderTables()).hasSize(tableGroup.getOrderTables().size())
                .extracting("tableGroup")
                .allMatch(Objects::isNull);
    }
}
