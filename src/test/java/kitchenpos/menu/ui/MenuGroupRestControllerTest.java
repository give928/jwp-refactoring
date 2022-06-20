package kitchenpos.menu.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.annotation.MockMvcEncodingConfiguration;
import kitchenpos.menu.application.MenuGroupService;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.dto.MenuGroupRequest;
import kitchenpos.menu.dto.MenuGroupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcEncodingConfiguration
@WebMvcTest(MenuGroupRestController.class)
class MenuGroupRestControllerTest {
    private static final String URL = "/api/menu-groups";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuGroupService menuGroupService;

    private MenuGroup menuGroup1;
    private MenuGroup menuGroup2;

    @BeforeEach
    void setUp() {
        menuGroup1 = new MenuGroup(1L, "메뉴그룹1");
        menuGroup2 = new MenuGroup(2L, "메뉴그룹2");
    }

    @DisplayName("메뉴 그룹을 등록하고 등록한 메뉴 그룹을 반환한다.")
    @Test
    void create() throws Exception {
        // given
        MenuGroupRequest menuGroupRequest = new MenuGroupRequest(menuGroup1.getName());
        MenuGroupResponse menuGroupResponse = MenuGroupResponse.from(menuGroup1);

        given(menuGroupService.create(any())).willReturn(menuGroupResponse);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(menuGroupRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(menuGroupResponse)));
    }

    @DisplayName("메뉴 그룹의 전체 목록을 조회한다.")
    @Test
    void list() throws Exception {
        // given
        List<MenuGroupResponse> menuGroups = Arrays.asList(MenuGroupResponse.from(menuGroup1),
                                                           MenuGroupResponse.from(menuGroup2));

        given(menuGroupService.list()).willReturn(menuGroups);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(menuGroups)));
    }
}
