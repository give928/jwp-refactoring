package kitchenpos.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NameTest {
    @DisplayName("이름을 생성한다.")
    @Test
    void create() {
        // when
        Name name = Name.from("이름");

        // then
        assertThat(name).isEqualTo(Name.from("이름"));
    }
}
