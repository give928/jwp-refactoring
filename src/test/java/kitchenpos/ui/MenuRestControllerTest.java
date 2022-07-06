package kitchenpos.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.annotation.MockMvcEncodingConfiguration;
import kitchenpos.application.MenuService;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
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

    private Menu savedMenu;
    private MenuProduct savedMenuProduct1;
    private MenuProduct savedMenuProduct2;

    @BeforeEach
    void setUp() {
        MenuGroup menuGroup1 = new MenuGroup(1L, "메뉴그룹1");
        Product product1 = new Product(1L, "음식1", BigDecimal.ONE);
        Product product2 = new Product(2L, "음식2", BigDecimal.ONE);
        savedMenuProduct1 = new MenuProduct(product1.getId(), 1);
        savedMenuProduct2 = new MenuProduct(product2.getId(), 1);
        savedMenu = new Menu(1L, "메뉴1", BigDecimal.valueOf(2L), menuGroup1.getId(),
                             Arrays.asList(savedMenuProduct1, savedMenuProduct2));
    }

    @DisplayName("메뉴를 등록하고 등록한 메뉴와 메뉴 상품을 반환한다.")
    @Test
    void create() throws Exception {
        // given
        Menu menu = new Menu("메뉴1", BigDecimal.valueOf(2L), 1L,
                             Arrays.asList(savedMenuProduct1, savedMenuProduct2));

        given(menuService.create(menu)).willReturn(savedMenu);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(menu)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(savedMenu)));
    }

    @DisplayName("메뉴와 메뉴 상품의 전체 목록을 조회한다.")
    @Test
    void list() throws Exception {
        // given
        MenuGroup menuGroup2 = new MenuGroup(2L, "메뉴그룹2");
        Product product3 = new Product(3L, "음식3", BigDecimal.ONE);
        Product product4 = new Product(4L, "음식4", BigDecimal.ONE);
        MenuProduct menuProduct3 = new MenuProduct(product3.getId(), 1);
        MenuProduct menuProduct4 = new MenuProduct(product4.getId(), 1);
        Menu menu2 = new Menu(2L, "메뉴2", BigDecimal.valueOf(2L), menuGroup2.getId(), Arrays.asList(menuProduct3, menuProduct4));
        List<Menu> menus = Arrays.asList(savedMenu, menu2);

        given(menuService.list()).willReturn(menus);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(menus)));
    }
}
