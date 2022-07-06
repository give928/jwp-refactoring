package kitchenpos.product.domain;

import kitchenpos.common.exception.RequiredNameException;
import kitchenpos.common.exception.RequiredPriceException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static kitchenpos.product.ProductFixtures.aProduct1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {
    @DisplayName("상품을 생성한다.")
    @Test
    void create() {
        // when
        Product product = aProduct1();

        // then
        assertThat(product).isEqualTo(aProduct1());
    }

    @DisplayName("상품의 이름은 필수이다.")
    @Test
    void cannotCreateNullName() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Product.of(1L, null, BigDecimal.ONE);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredNameException.class)
                .hasMessageContaining(RequiredNameException.MESSAGE);
    }

    @DisplayName("상품의 가격은 필수이다.")
    @Test
    void cannotCreateNullPrice() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Product.of(1L, "음식1", null);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredPriceException.class)
                .hasMessageContaining(RequiredPriceException.MESSAGE);
    }
}
