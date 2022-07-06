package kitchenpos;

import kitchenpos.table.application.TableService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TableApplicationTest {
    @Autowired
    private TableService tableService;

    @Test
    void contextLoads() {
        assertThat(tableService).isNotNull();
    }
}
