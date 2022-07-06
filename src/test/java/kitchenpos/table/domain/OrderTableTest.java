package kitchenpos.table.domain;

import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.product.domain.Product;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTableTest {
    @DisplayName("주문 테이블을 생성한다.")
    @Test
    void create() {
        // when
        OrderTable orderTable = OrderTable.of(1L, null, 1, true);

        // then
        assertThat(orderTable).isEqualTo(OrderTable.of(1L, null, 1, true));
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void clearTableGroup() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 1, true);

        // when
        orderTable.clearTableGroup();

        // then
        assertThat(orderTable.getTableGroup()).isNull();
    }

    @DisplayName("조리중, 식사중인 경우 단체 지정을 해제할 수 없다.")
    @Test
    void cannotClearTableGroup() {
        // given
        Menu menu = createMenu();
        OrderTable orderTable = OrderTable.of(1L, null, 1, true);
        orderTable.changeTableGroup(new TableGroup());
        orderTable.addOrder(Order.of(1L, orderTable, Collections.singletonList(OrderLineItem.of(menu, 1L))));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = orderTable::clearTableGroup;

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("단체 지정을 한다.")
    @Test
    void changeTableGroup() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 1, true);
        TableGroup tableGroup = new TableGroup();

        // when
        orderTable.changeTableGroup(tableGroup);

        // then
        assertThat(orderTable.isEmpty()).isFalse();
        assertThat(orderTable.getTableGroup()).isNotNull();
    }

    @DisplayName("비어있지 않은 테이블은 단체 지정을 할 수 없다.")
    @Test
    void cannotChangeTableGroup() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 1, false);
        TableGroup tableGroup = new TableGroup();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.changeTableGroup(tableGroup);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("방문 손님 수를 변경한다.")
    @Test
    void changeNumberOfGuests() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 0, false);

        // when
        orderTable.changeNumberOfGuests(1);

        // then
        assertThat(orderTable.getNumberOfGuests()).isEqualTo(1);
    }

    @DisplayName("빈 테이블은 방문 손님 수를 변경할 수 없다.")
    @Test
    void cannotChangeNumberOfGuests() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 0, true);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.changeNumberOfGuests(1);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 테이블 여부를 변경한다.")
    @Test
    void changeEmpty() {
        // given
        OrderTable orderTable = OrderTable.of(1L, null, 0, false);

        // when
        orderTable.changeEmpty(true);

        // then
        assertThat(orderTable.isEmpty()).isTrue();
    }

    @DisplayName("단체 지정된 주문 테이블은 빈 테이블 여부를 변경할 수 없다.")
    @Test
    void cannotChangeEmptyNotNullTableGroup() {
        // given
        OrderTable orderTable = OrderTable.of(1L, new TableGroup(), 0, false);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.changeEmpty(true);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 빈 테이블 여부를 변경할 수 없다.")
    @Test
    void cannotChangeEmptyOrdersInCookingOrMeal() {
        // given
        Menu menu = createMenu();
        OrderTable orderTable = OrderTable.of(1L, null, 1, false);
        orderTable.addOrder(Order.of(1L, orderTable, Collections.singletonList(OrderLineItem.of(menu, 1L))));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderTable.changeEmpty(true);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    private Menu createMenu() {
        Product product = Product.of(1L, "음식1", BigDecimal.ONE);
        List<MenuProduct> menuProducts = Collections.singletonList(MenuProduct.of(1L, null, product, 1L));
        return Menu.of(1L, "메뉴1", BigDecimal.ONE, MenuGroup.of(1L, "메뉴그룹1"), menuProducts);
    }
}
