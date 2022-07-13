package kitchenpos;

import kitchenpos.common.SpringTest;
import kitchenpos.menu.application.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class MenuApplicationTest extends SpringTest {
    @Autowired
    private MenuService menuService;

    @Test
    void contextLoads() {
        assertThat(menuService).isNotNull();
    }
}
