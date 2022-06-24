package kitchenpos.menu.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.annotation.MockMvcEncodingConfiguration;
import kitchenpos.menu.application.MenuService;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.menu.domain.MenuProducts;
import kitchenpos.menu.dto.MenuProductRequest;
import kitchenpos.menu.dto.MenuRequest;
import kitchenpos.menu.dto.MenuResponse;
import kitchenpos.product.domain.Product;
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

    private Menu menu1;
    private Menu menu2;

    @BeforeEach
    void setUp() {
        Long savedMenuId1 = 1L;
        Product product1 = Product.of(1L, "음식1", BigDecimal.ONE);
        Product product2 = Product.of(2L, "음식2", BigDecimal.ONE);
        MenuGroup menuGroup = MenuGroup.of(1L, "메뉴그룹1");
        menu1 = Menu.of(savedMenuId1, "메뉴1", BigDecimal.valueOf(2L), menuGroup,
                        MenuProducts.from(Arrays.asList(MenuProduct.of(1L, menu1, product1, 1),
                                                        MenuProduct.of(2L, menu1, product2, 1))));

        Long savedMenuId2 = 2L;
        Product product3 = Product.of(3L, "음식1", BigDecimal.ONE);
        Product product4 = Product.of(4L, "음식2", BigDecimal.ONE);
        menu2 = Menu.of(savedMenuId2, "메뉴2", BigDecimal.valueOf(2L), menuGroup,
                        MenuProducts.from(Arrays.asList(MenuProduct.of(3L, menu2, product3, 1),
                                                        MenuProduct.of(4L, menu2, product4, 1))));
    }

    @DisplayName("메뉴를 등록하고 등록한 메뉴와 메뉴 상품을 반환한다.")
    @Test
    void create() throws Exception {
        // given
        MenuRequest menuRequest = createMenuRequestBy(menu1);
        MenuResponse menuResponse = MenuResponse.from(menu1);

        given(menuService.create(any())).willReturn(menuResponse);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(menuRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(menuResponse)));
    }

    @DisplayName("메뉴와 메뉴 상품의 전체 목록을 조회한다.")
    @Test
    void list() throws Exception {
        // given
        List<MenuResponse> menuResponses = Arrays.asList(MenuResponse.from(menu1),
                                                         MenuResponse.from(menu2));

        given(menuService.list()).willReturn(menuResponses);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(menuResponses)));
    }

    private MenuRequest createMenuRequestBy(Menu menu) {
        return new MenuRequest(menu.getName(), menu.getPrice(), menu.getMenuGroup().getId(),
                               menu.getMenuProducts().stream()
                                       .map(menuProduct -> new MenuProductRequest(
                                               menuProduct.getProduct().getId(),
                                               menuProduct.getQuantity()))
                                       .collect(Collectors.toList()));
    }
}
