package kitchenpos.menu.domain;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static kitchenpos.Fixtures.aMenu1;
import static kitchenpos.Fixtures.aMenuProduct1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuProductTest {
    @DisplayName("메뉴 상품을 생성한다.")
    @Test
    void create() {
        // when
        MenuProduct menuProduct = aMenuProduct1();

        // then
        assertThat(menuProduct).isEqualTo(MenuProduct.of(1L, null, 1L, 1L));
    }

    @DisplayName("메뉴 상품의 상품은 필수이다.")
    @Test
    void cannotCreateNullProduct() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> MenuProduct.of(1L, null, null, 1L);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(NullPointerException.class);
    }

    @DisplayName("메뉴 상품에 해당하는 메뉴를 설정한다.")
    @Test
    void initMenu() {
        // given
        MenuProduct menuProduct = aMenuProduct1();
        Menu menu = aMenu1();

        // when
        menuProduct.initMenu(menu);

        // then
        assertThat(menuProduct.getMenu()).isEqualTo(menu);
    }
}
