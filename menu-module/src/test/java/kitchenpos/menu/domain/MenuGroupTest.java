package kitchenpos.menu.domain;

import kitchenpos.common.exception.RequiredNameException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static kitchenpos.menu.MenuFixtures.aMenuGroup1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuGroupTest {
    @DisplayName("메뉴 그룹을 생성한다.")
    @Test
    void create() {
        // when
        MenuGroup menuGroup = aMenuGroup1();

        // then
        assertThat(menuGroup).isEqualTo(aMenuGroup1());
    }

    @DisplayName("메뉴 그룹의 이름은 필수이다.")
    @Test
    void cannotCreateNullName() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> MenuGroup.of(1L, null);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredNameException.class)
                .hasMessageContaining(RequiredNameException.MESSAGE);
    }
}
