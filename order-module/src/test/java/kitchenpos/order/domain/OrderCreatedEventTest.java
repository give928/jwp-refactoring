package kitchenpos.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static kitchenpos.order.OrderFixtures.aOrder1;
import static org.assertj.core.api.Assertions.assertThat;

class OrderCreatedEventTest {
    @DisplayName("주문 생성 이벤트 도메인을 생성한다.")
    @Test
    void create() {
        // given
        Order order = aOrder1().build();

        // when
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.from(order);

        // then
        assertThat(orderCreatedEvent.getOrderId()).isEqualTo(order.getId());
    }
}
