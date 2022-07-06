package kitchenpos.menu.domain;

import kitchenpos.product.domain.Product;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuProductTest {
    @DisplayName("메뉴 상품을 생성한다.")
    @Test
    void create() {
        // given
        Product product1 = Product.of(1L, "음식1", BigDecimal.ONE);

        // when
        MenuProduct menuProduct = MenuProduct.of(1L, null, product1, 1L);

        // then
        assertThat(menuProduct).isEqualTo(MenuProduct.of(1L, null, product1, 1L));
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
        Product product1 = Product.of(1L, "음식1", BigDecimal.ONE);
        MenuProduct menuProduct = MenuProduct.of(1L, null, Product.of(1L, "음식1", BigDecimal.ONE), 1L);
        Menu menu = Menu.of(1L, "메뉴1", BigDecimal.ONE, MenuGroup.of(1L, "메뉴그룹1"),
                            Collections.singletonList(MenuProduct.of(1L, null, product1, 1L)));

        // when
        menuProduct.initMenu(menu);

        // then
        assertThat(menuProduct.getMenu()).isEqualTo(menu);
    }
}
