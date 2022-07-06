package kitchenpos.application;

import kitchenpos.dao.MenuGroupDao;
import kitchenpos.domain.MenuGroup;
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
    private MenuGroupDao menuGroupDao;

    @InjectMocks
    private MenuGroupService menuGroupService;

    private MenuGroup savedMenuGroup;

    @BeforeEach
    void setUp() {
        savedMenuGroup = new MenuGroup(1L, "메뉴그룹1");
    }

    @DisplayName("메뉴 그룹을 등록하고 등록한 메뉴 그룹을 반환한다.")
    @Test
    void create() {
        // given
        MenuGroup menuGroup = new MenuGroup("메뉴그룹1");

        given(menuGroupDao.save(menuGroup)).willReturn(savedMenuGroup);

        // when
        MenuGroup savedMenuGroup = menuGroupService.create(menuGroup);

        // then
        assertThat(savedMenuGroup).isEqualTo(this.savedMenuGroup);
    }

    @DisplayName("메뉴 그룹의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        List<MenuGroup> menuGroups = Arrays.asList(savedMenuGroup, new MenuGroup(2L, "메뉴그룹2"));

        given(menuGroupDao.findAll()).willReturn(menuGroups);

        // when
        List<MenuGroup> findMenuGroups = menuGroupService.list();

        // then
        assertThat(findMenuGroups).containsExactlyElementsOf(menuGroups);
    }
}
