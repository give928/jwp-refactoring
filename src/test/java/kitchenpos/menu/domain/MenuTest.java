package kitchenpos.menu.domain;

import kitchenpos.product.domain.Product;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuTest {
    private MenuGroup menuGroup1;
    private List<MenuProduct> menuProducts;

    @BeforeEach
    void setUp() {
        menuGroup1 = MenuGroup.of(1L, "메뉴그룹1");
        Product product1 = Product.of(1L, "음식1", BigDecimal.ONE);
        menuProducts = Collections.singletonList(MenuProduct.of(1L, null, product1, 1L));
    }

    @DisplayName("메뉴를 생성한다.")
    @Test
    void create() {
        // when
        Menu menu = Menu.of(1L, "메뉴1", BigDecimal.ONE, menuGroup1, menuProducts);

        // then
        assertThat(menu).isEqualTo(Menu.of(1L, "메뉴1", BigDecimal.ONE, menuGroup1, menuProducts));
    }

    @DisplayName("메뉴명은 필수이다.")
    @Test
    void cannotCreateNullName() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Menu.of(1L, null, BigDecimal.ONE, menuGroup1, menuProducts);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("가격은 필수이다.")
    @Test
    void cannotCreateNullPrice() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Menu.of(1L, "메뉴1", null, menuGroup1, menuProducts);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴 그룹은 필수이다.")
    @Test
    void cannotCreateNullMenuGroup() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Menu.of(1L, "메뉴1", BigDecimal.ONE, null, menuProducts);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(NullPointerException.class);
    }

    @DisplayName("메뉴 상품 컬렉션을 설정한다.")
    @Test
    void initMenuProducts() {
        // given
        Menu menu = Menu.of(1L, "메뉴1", BigDecimal.ZERO, menuGroup1, null);

        // when
        menu.initMenuProducts(MenuProducts.from(menuProducts));

        // then
        assertThat(menu.getMenuProducts()).isEqualTo(menuProducts);
    }
}
