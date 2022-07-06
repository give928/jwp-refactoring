package kitchenpos.menu.domain;

import kitchenpos.product.domain.Product;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuProductsTest {
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = Product.of(1L, "음식1", BigDecimal.ONE);
        product2 = Product.of(2L, "음식2", BigDecimal.valueOf(2L));
    }

    @DisplayName("메뉴 상품 일급 컬렉션을 생성한다.")
    @Test
    void create() {
        // when
        MenuProducts menuProducts = MenuProducts.from(Arrays.asList(MenuProduct.of(1L, null, product1, 1L),
                                                                    MenuProduct.of(2L, null, product2, 1L)));

        // then
        assertThat(menuProducts).isEqualTo(MenuProducts.from(Arrays.asList(MenuProduct.of(1L, null, product1, 1L),
                                                                           MenuProduct.of(2L, null, product2, 1L))));
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
        Menu menu = Menu.of(1L, "메뉴1", BigDecimal.valueOf(0L), MenuGroup.of(1L, "메뉴그룹1"), null);
        MenuProducts menuProducts = MenuProducts.from(Arrays.asList(MenuProduct.of(1L, null, product1, 1L),
                                                                    MenuProduct.of(2L, null, product2, 1L)));

        // when
        MenuProducts settingMenuProducts = menuProducts.initMenu(menu);

        // then
        assertThat(settingMenuProducts.get()).hasSize(menuProducts.get().size());
        assertThat(settingMenuProducts.get()).extracting("menu")
                .containsExactly(menu, menu);
        assertThat(menu.getMenuProducts()).isEqualTo(menuProducts.get());
    }
}
