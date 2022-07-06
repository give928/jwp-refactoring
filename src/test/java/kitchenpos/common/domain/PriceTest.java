package kitchenpos.common.domain;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceTest {
    @DisplayName("가격을 생성한다.")
    @Test
    void create() {
        // when
        Price price = Price.from(BigDecimal.ZERO);

        // then
        assertThat(price).isEqualTo(Price.from(BigDecimal.ZERO));
    }

    @DisplayName("가격은 필수로 입력해야 한다.")
    @Test
    void cannotCreateNullPrice() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Price.from(null);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("0원 미만의 가격은 생성할 수 없다.")
    @Test
    void cannotCreateNegativePrice() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> Price.from(BigDecimal.valueOf(-1));

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
}
