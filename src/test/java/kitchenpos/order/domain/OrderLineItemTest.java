package kitchenpos.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderLineItemTest {
    @DisplayName("주문 항목을 생성한다.")
    @Test
    void create() {
        // given

        // when
        OrderLineItem orderLineItem = aOrderLineItem1();

        // then
        assertThat(orderLineItem).isEqualTo(aOrderLineItem1());
    }

    @DisplayName("주문 항목에 주문을 설정한다.")
    @Test
    void initOrder() {
        // given
        Order order = Order.of(1L, aOrderTable1().getId(), Collections.emptyList(), aOrderValidator());
        OrderLineItem orderLineItem = aOrderLineItem1();

        // when
        orderLineItem.initOrder(order);

        // then
        assertThat(orderLineItem.getOrder()).isEqualTo(order);
    }
}
