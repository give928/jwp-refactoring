package kitchenpos.menu.domain;

import kitchenpos.menu.dto.ProductResponse;
import kitchenpos.menu.exception.InvalidMenuPriceException;
import kitchenpos.menu.exception.ProductNotFoundException;
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

import static kitchenpos.menu.MenuFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MenuValidatorTest {
    @Mock
    private ProductClient productClient;

    @InjectMocks
    private MenuValidator menuValidator;

    private ProductResponse productResponse1;
    private ProductResponse productResponse2;
    private List<Long> productIds;
    private Menu menu;

    @BeforeEach
    void setUp() {
        productResponse1 = aProductResponse1();
        productResponse2 = aProductResponse2();
        productIds = Arrays.asList(productResponse1.getId(), productResponse2.getId());
        menu = aMenu1().build();
    }

    @DisplayName("메뉴의 유효성을 확인한다.")
    @Test
    void create() {
        // given
        given(productClient.getProducts(productIds))
                .willReturn(Arrays.asList(productResponse1, productResponse2));

        // when
        boolean valid = menuValidator.create(menu);

        // then
        assertThat(valid).isTrue();

        then(productClient).should(times(1))
                .getProducts(productIds);
    }

    @DisplayName("등록된 상품만 메뉴 상품으로 등록할 수 있다.")
    @Test
    void notExistsProduct() {
        // given
        given(productClient.getProducts(productIds))
                .willReturn(Collections.singletonList(productResponse1));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuValidator.create(menu);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(ProductNotFoundException.MESSAGE);

        then(productClient).should(times(1))
                .getProducts(productIds);
    }

    @DisplayName("메뉴의 가격은 메뉴 상품들의 금액의 합 이하만 가능하다.")
    @Test
    void invalidPrice() {
        // given
        BigDecimal price = productResponse1.getPrice()
                .add(productResponse2.getPrice())
                .add(BigDecimal.ONE);
        Menu menu = Menu.of(null, "메뉴1", price, aMenuGroup1(),
                            aMenuProducts1().get(), aMenuValidator());

        given(productClient.getProducts(productIds))
                .willReturn(Arrays.asList(productResponse1, productResponse2));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuValidator.create(menu);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(InvalidMenuPriceException.class)
                .hasMessageContaining(InvalidMenuPriceException.MESSAGE);

        then(productClient).should(times(1))
                .getProducts(productIds);
    }
}
