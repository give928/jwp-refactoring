package kitchenpos.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static kitchenpos.order.OrderFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderLineItemsTest {
    @DisplayName("주문 항목 컬렉션을 생성한다.")
    @Test
    void create() {
        // when
        OrderLineItems orderLineItems = aOrderLineItems1();

        // then
        assertThat(orderLineItems).isEqualTo(aOrderLineItems1());
    }

    @DisplayName("주문 항목 컬렉션에 주문을 설정한다.")
    @Test
    void initOrder() {
        // given
        Order order = Order.of(1L, aOrderTable1(), Collections.emptyList(), aOrderValidator(), aOrderEventPublisher());
        OrderLineItems orderLineItems = aOrderLineItems1();

        // when
        orderLineItems.initOrder(order);

        // then
        assertThat(orderLineItems.get()).extracting("order")
                .containsExactly(order, order);
    }
}
