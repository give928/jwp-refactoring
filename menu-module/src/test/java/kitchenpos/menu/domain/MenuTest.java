package kitchenpos.menu.domain;

import kitchenpos.common.exception.RequiredNameException;
import kitchenpos.common.exception.RequiredPriceException;
import kitchenpos.menu.exception.RequiredMenuGroupException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static kitchenpos.menu.MenuFixtures.aMenuGroup1;
import static kitchenpos.menu.MenuFixtures.aMenuProducts1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MenuTest {
    @Mock
    private MenuValidator menuValidator;

    private MenuGroup menuGroup1;
    private List<MenuProduct> menuProducts;

    @BeforeEach
    void setUp() {
        menuGroup1 = aMenuGroup1();
        menuProducts = aMenuProducts1().get();
    }

    @DisplayName("메뉴를 생성한다.")
    @Test
    void create() {
        // given
        given(menuValidator.create(any())).willReturn(true);

        // when
        Menu menu = Menu.of(1L, "메뉴1", BigDecimal.ONE, menuGroup1, menuProducts, menuValidator);

        // then
        assertThat(menu).isEqualTo(Menu.of(1L, "메뉴1", BigDecimal.ONE, menuGroup1, menuProducts, menuValidator));
    }

    @DisplayName("메뉴명은 필수이다.")
    @Test
    void cannotCreateNullName() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Menu.of(1L, null, BigDecimal.ONE, menuGroup1, menuProducts, menuValidator);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredNameException.class)
                .hasMessageContaining(RequiredNameException.MESSAGE);
    }

    @DisplayName("가격은 필수이다.")
    @Test
    void cannotCreateNullPrice() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Menu.of(1L, "메뉴1", null, menuGroup1, menuProducts, menuValidator);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredPriceException.class)
                .hasMessageContaining(RequiredPriceException.MESSAGE);
    }

    @DisplayName("메뉴 그룹은 필수이다.")
    @Test
    void cannotCreateNullMenuGroup() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Menu.of(1L, "메뉴1", BigDecimal.ONE, null, menuProducts, menuValidator);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredMenuGroupException.class)
                .hasMessageContaining(RequiredMenuGroupException.MESSAGE);
    }

    @DisplayName("메뉴 상품 컬렉션을 설정한다.")
    @Test
    void initMenuProducts() {
        // given
        Menu menu = Menu.of(1L, "메뉴1", BigDecimal.ZERO, menuGroup1, null, menuValidator);

        // when
        menu.initMenuProducts(MenuProducts.from(menuProducts));

        // then
        assertThat(menu.getMenuProducts()).isEqualTo(menuProducts);
    }
}
