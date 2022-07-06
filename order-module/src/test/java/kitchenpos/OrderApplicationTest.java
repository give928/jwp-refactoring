package kitchenpos;

import kitchenpos.order.application.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderApplicationTest {
    @Autowired
    private OrderService orderService;

    @Test
    void contextLoads() {
        assertThat(orderService).isNotNull();
    }
}
