package kitchenpos.menu.application;

import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.domain.MenuGroupRepository;
import kitchenpos.menu.dto.MenuGroupRequest;
import kitchenpos.menu.dto.MenuGroupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceTest {
    @Mock
    private MenuGroupRepository menuGroupRepository;

    @InjectMocks
    private MenuGroupService menuGroupService;

    private MenuGroup menuGroup1;
    private MenuGroup menuGroup2;

    @BeforeEach
    void setUp() {
        menuGroup1 = MenuGroup.of(1L, "메뉴그룹1");
        menuGroup2 = MenuGroup.of(2L, "메뉴그룹2");
    }

    @DisplayName("메뉴 그룹을 등록하고 등록한 메뉴 그룹을 반환한다.")
    @Test
    void create() {
        // given
        MenuGroupRequest menuGroupRequest = new MenuGroupRequest(menuGroup1.getName());

        given(menuGroupRepository.save(MenuGroup.of(menuGroupRequest.getName()))).willReturn(menuGroup1);

        // when
        MenuGroupResponse menuGroupResponse = menuGroupService.create(menuGroupRequest);

        // then
        assertThat(menuGroupResponse.getId()).isEqualTo(menuGroup1.getId());
        assertThat(menuGroupResponse.getName()).isEqualTo(menuGroup1.getName());
    }

    @DisplayName("메뉴 그룹의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        given(menuGroupRepository.findAll()).willReturn(Arrays.asList(menuGroup1, menuGroup2));

        // when
        List<MenuGroupResponse> menuGroupResponses = menuGroupService.list();

        // then
        assertThat(menuGroupResponses).extracting("id").containsExactly(menuGroup1.getId(), menuGroup2.getId());
        assertThat(menuGroupResponses).extracting("name").containsExactly(menuGroup1.getName(), menuGroup2.getName());
    }
}
