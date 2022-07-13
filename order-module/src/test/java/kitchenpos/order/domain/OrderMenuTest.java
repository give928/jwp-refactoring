package kitchenpos.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMenuTest {
    @DisplayName("주문 메뉴를 생성한다.")
    @Test
    void create() {
        // when
        OrderMenu orderMenu = OrderMenu.of(1L, "음식1", 1_000L);

        // then
        assertThat(orderMenu.getMenuId()).isEqualTo(1L);
        assertThat(orderMenu.getMenuName()).isEqualTo("음식1");
        assertThat(orderMenu.getMenuPrice()).isEqualTo(BigDecimal.valueOf(1_000L));
    }
}
