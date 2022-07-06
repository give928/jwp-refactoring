package kitchenpos.menu.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.common.annotation.MockMvcEncodingConfiguration;
import kitchenpos.menu.application.MenuService;
import kitchenpos.menu.dto.MenuProductRequest;
import kitchenpos.menu.dto.MenuProductResponse;
import kitchenpos.menu.dto.MenuRequest;
import kitchenpos.menu.dto.MenuResponse;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcEncodingConfiguration
@WebMvcTest(MenuRestController.class)
class MenuRestControllerTest {
    private static final String URL = "/api/menus";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    private MenuResponse menuResponse1;
    private MenuResponse menuResponse2;

    @BeforeEach
    void setUp() {
        Long menuId1 = 1L;
        menuResponse1 = new MenuResponse(menuId1, "메뉴1", BigDecimal.valueOf(2), 1L,
                                         Arrays.asList(new MenuProductResponse(1L, menuId1, 1L, 1),
                                                       new MenuProductResponse(2L, menuId1, 2L, 1)));
        Long menuId2 = 2L;
        menuResponse2 = new MenuResponse(menuId2, "메뉴2", BigDecimal.valueOf(2), 1L,
                                         Arrays.asList(new MenuProductResponse(3L, menuId2, 3L, 1),
                                                       new MenuProductResponse(4L, menuId2, 4L, 1)));
    }

    @DisplayName("메뉴를 등록하고 등록한 메뉴와 메뉴 상품을 반환한다.")
    @Test
    void create() throws Exception {
        // given
        MenuRequest menuRequest = createMenuRequestBy(menuResponse1);

        given(menuService.create(any())).willReturn(menuResponse1);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(menuRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(menuResponse1)));
    }

    @DisplayName("메뉴와 메뉴 상품의 전체 목록을 조회한다.")
    @Test
    void list() throws Exception {
        // given
        List<MenuResponse> menuResponses = Arrays.asList(menuResponse1, menuResponse2);

        given(menuService.list()).willReturn(menuResponses);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(menuResponses)));
    }

    private MenuRequest createMenuRequestBy(MenuResponse menuResponse) {
        return new MenuRequest(menuResponse.getName(), menuResponse.getPrice(), menuResponse.getMenuGroupId(),
                               menuResponse.getMenuProducts().stream()
                                       .map(menuProduct -> new MenuProductRequest(
                                               menuProduct.getProductId(),
                                               menuProduct.getQuantity()))
                                       .collect(Collectors.toList()));
    }
}
