package kitchenpos.menu.domain;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static kitchenpos.Fixtures.aMenu1;
import static kitchenpos.Fixtures.aMenuProducts1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuProductsTest {
    @DisplayName("메뉴 상품 일급 컬렉션을 생성한다.")
    @Test
    void create() {
        // when
        MenuProducts menuProducts = aMenuProducts1();

        // then
        assertThat(menuProducts).isEqualTo(MenuProducts.from(Arrays.asList(MenuProduct.of(1L, null, 1L, 1L),
                                                                           MenuProduct.of(2L, null, 2L, 1L))));
    }

    @DisplayName("메뉴 상품 컬렉션은 필수이다.")
    @Test
    void cannotCreateNullCollection() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> MenuProducts.from(null);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(NullPointerException.class);
    }

    @DisplayName("메뉴 상품들에 메뉴를 설정한다.")
    @Test
    void initMenu() {
        // given
        Menu menu = aMenu1();
        MenuProducts menuProducts = aMenuProducts1();

        // when
        MenuProducts settingMenuProducts = menuProducts.initMenu(menu);

        // then
        assertThat(settingMenuProducts.get()).hasSize(menuProducts.get().size());
        assertThat(settingMenuProducts.get()).extracting("menu")
                .containsExactly(menu, menu);
        assertThat(menu.getMenuProducts()).isEqualTo(menuProducts.get());
    }
}
