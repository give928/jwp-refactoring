package kitchenpos;

import kitchenpos.common.SpringTest;
import kitchenpos.table.application.TableService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class TableApplicationTest extends SpringTest {
    @Autowired
    private TableService tableService;

    @Test
    void contextLoads() {
        assertThat(tableService).isNotNull();
    }
}
