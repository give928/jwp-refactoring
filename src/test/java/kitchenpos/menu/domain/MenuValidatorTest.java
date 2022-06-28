package kitchenpos.menu.domain;

import kitchenpos.menu.exception.InvalidMenuPriceException;
import kitchenpos.product.domain.Product;
import kitchenpos.product.domain.ProductRepository;
import kitchenpos.product.exception.ProductNotFoundException;
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

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MenuValidatorTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private MenuValidator menuValidator;

    private Product product1;
    private Product product2;
    private List<Long> productIds;
    private Menu menu;

    @BeforeEach
    void setUp() {
        product1 = aProduct1();
        product2 = aProduct2();
        productIds = Arrays.asList(product1.getId(), product2.getId());
        menu = aMenu1().build();
    }

    @DisplayName("메뉴의 유효성을 확인한다.")
    @Test
    void create() {
        // given
        given(productRepository.findByIdIn(productIds))
                .willReturn(Arrays.asList(product1, product2));

        // when
        boolean valid = menuValidator.create(menu);

        // then
        assertThat(valid).isTrue();

        then(productRepository).should(times(1))
                .findByIdIn(productIds);
    }

    @DisplayName("등록된 상품만 메뉴 상품으로 등록할 수 있다.")
    @Test
    void notExistsProduct() {
        // given
        given(productRepository.findByIdIn(productIds))
                .willReturn(Collections.singletonList(product1));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuValidator.create(menu);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(ProductNotFoundException.MESSAGE);

        then(productRepository).should(times(1))
                .findByIdIn(productIds);
    }

    @DisplayName("메뉴의 가격은 메뉴 상품들의 금액의 합 이하만 가능하다.")
    @Test
    void invalidPrice() {
        // given
        Menu menu = Menu.of(null, "메뉴1", BigDecimal.valueOf(4), aMenuGroup1(),
                            aMenuProducts1().get(), aMenuValidator());

        given(productRepository.findByIdIn(productIds))
                .willReturn(Arrays.asList(product1, product2));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuValidator.create(menu);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(InvalidMenuPriceException.class)
                .hasMessageContaining(InvalidMenuPriceException.MESSAGE);

        then(productRepository).should(times(1))
                .findByIdIn(productIds);
    }
}
