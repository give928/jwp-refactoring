package kitchenpos.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {
    @DisplayName("조리중이거나 식사인지 주문 상태를 확인한다.")
    @Test
    void isCookingOrMeal() {
        // when
        boolean cooking = OrderStatus.COOKING.isCookingOrMeal();
        boolean meal = OrderStatus.MEAL.isCookingOrMeal();

        // then
        assertThat(cooking).isTrue();
        assertThat(meal).isTrue();
    }
}
