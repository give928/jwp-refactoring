package kitchenpos;

import kitchenpos.menu.application.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class MenuApplicationTest {
    @Autowired
    private MenuService menuService;

    @Test
    void contextLoads() {
        assertThat(menuService).isNotNull();
    }
}
