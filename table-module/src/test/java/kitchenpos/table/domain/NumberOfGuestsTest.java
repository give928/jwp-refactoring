package kitchenpos.table.domain;

import kitchenpos.table.exception.InvalidNumberOfGuestsException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumberOfGuestsTest {
    @DisplayName("방문 손님 수를 생성한다.")
    @Test
    void create() {
        // when
        NumberOfGuests numberOfGuests = NumberOfGuests.from(1);

        // then
        assertThat(numberOfGuests).isEqualTo(NumberOfGuests.from(1));
    }

    @DisplayName("방문 손님 수는 0명 이상만 가능하다.")
    @Test
    void cannotCreateNegative() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> NumberOfGuests.from(-1);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(InvalidNumberOfGuestsException.class)
                .hasMessageContaining(InvalidNumberOfGuestsException.MESSAGE);
    }

    @DisplayName("방문 손님 수 인스턴스를 캐싱한다.")
    @Test
    void instanceCaching() {
        // when
        NumberOfGuests numberOfGuests = NumberOfGuests.from(1);

        // then
        assertThat(numberOfGuests).isSameAs(NumberOfGuests.from(1));
    }
}
