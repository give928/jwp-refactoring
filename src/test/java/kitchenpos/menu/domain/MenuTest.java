package kitchenpos.menu.domain;

import kitchenpos.product.domain.Product;
import kitchenpos.product.domain.ProductRepository;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MenuTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
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
        given(productRepository.findByIdIn(menuProducts.stream()
                                                   .map(MenuProduct::getProductId)
                                                   .collect(Collectors.toList())))
                .willReturn(Arrays.asList(aProduct1(), aProduct2()));

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
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("가격은 필수이다.")
    @Test
    void cannotCreateNullPrice() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Menu.of(1L, "메뉴1", null, menuGroup1, menuProducts, menuValidator);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴 그룹은 필수이다.")
    @Test
    void cannotCreateNullMenuGroup() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Menu.of(1L, "메뉴1", BigDecimal.ONE, null, menuProducts, menuValidator);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(NullPointerException.class);
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
