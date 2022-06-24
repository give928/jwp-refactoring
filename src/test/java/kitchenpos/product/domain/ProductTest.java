package kitchenpos.product.domain;

import kitchenpos.common.domain.Name;
import kitchenpos.common.domain.Price;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {
    @DisplayName("상품을 생성한다.")
    @Test
    void create() {
        // when
        Product product = Product.of(1L, "음식1", BigDecimal.ONE);

        // then
        assertThat(product).isEqualTo(Product.of(1L, "음식1", BigDecimal.ONE));
    }

    @DisplayName("상품의 이름은 필수이다.")
    @Test
    void cannotCreateNullName() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Product.of(1L, null, BigDecimal.ONE);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품의 가격은 필수이다.")
    @Test
    void cannotCreateNullPrice() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Product.of(1L, "음식1", null);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
}
