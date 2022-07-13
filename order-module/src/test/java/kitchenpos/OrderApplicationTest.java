package kitchenpos;

import kitchenpos.common.SpringTest;
import kitchenpos.order.application.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class OrderApplicationTest extends SpringTest {
    @Autowired
    private OrderService orderService;

    @Test
    void contextLoads() {
        assertThat(orderService).isNotNull();
    }
}
