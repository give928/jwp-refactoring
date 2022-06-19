package kitchenpos.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.annotation.MockMvcEncodingConfiguration;
import kitchenpos.application.OrderService;
import kitchenpos.domain.*;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcEncodingConfiguration
@WebMvcTest(OrderRestController.class)
class OrderRestControllerTest {
    private static final String URL = "/api/orders";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    private Menu menu1;
    private Menu menu2;
    private Order order1;

    @BeforeEach
    void setUp() {
        Long menuId1 = 1L;
        Product product1 = new Product(1L, "음식1", BigDecimal.ONE);
        Product product2 = new Product(2L, "음식2", BigDecimal.ONE);
        MenuGroup menuGroup1 = new MenuGroup(1L, "메뉴그룹1");
        MenuProduct menuProduct1 = new MenuProduct(1L, menuId1, product1.getId(), 1);
        MenuProduct menuProduct2 = new MenuProduct(2L, menuId1, product2.getId(), 1);
        menu1 = new Menu(menuId1, "메뉴1", BigDecimal.valueOf(2L), menuGroup1.getId(), Arrays.asList(menuProduct1, menuProduct2));

        Long menuId2 = 2L;
        Product product3 = new Product(3L, "음식3", BigDecimal.ONE);
        Product product4 = new Product(4L, "음식4", BigDecimal.ONE);
        MenuGroup menuGroup2 = new MenuGroup(2L, "메뉴그룹2");
        MenuProduct menuProduct3 = new MenuProduct(3L, menuId2, product3.getId(), 1);
        MenuProduct menuProduct4 = new MenuProduct(4L, menuId2, product4.getId(), 1);
        menu2 = new Menu(menuId2, "메뉴2", BigDecimal.valueOf(2L), menuGroup2.getId(), Arrays.asList(menuProduct3, menuProduct4));

        Long orderId1 = 1L;
        OrderTable orderTable1 = new OrderTable(1L, null, 1, false);
        OrderLineItem orderLineItem1 = new OrderLineItem(1L, orderId1, menu1.getId(), 1);
        OrderLineItem orderLineItem2 = new OrderLineItem(2L, orderId1, menu2.getId(), 1);
        order1 = new Order(1L, orderTable1.getId(), OrderStatus.COOKING.name(), LocalDateTime.now(), Arrays.asList(orderLineItem1, orderLineItem2));
    }

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() throws Exception {
        // given
        OrderLineItem orderLineItem1 = new OrderLineItem(menu1.getId(), 1);
        OrderLineItem orderLineItem2 = new OrderLineItem(menu2.getId(), 1);
        Order order = new Order(order1.getOrderTableId(), order1.getOrderStatus(), order1.getOrderedTime(), Arrays.asList(orderLineItem1, orderLineItem2));

        given(orderService.create(order)).willReturn(order1);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(order)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(order1)));
    }

    @DisplayName("주문과 주문 항목의 전체 목록을 조회한다.")
    @Test
    void list() throws Exception {
        // given
        Order order2 = new Order(2L, 2L, OrderStatus.COOKING.name(), LocalDateTime.now(),
                                 Arrays.asList(new OrderLineItem(3L, 2L, 3L, 1),
                                               new OrderLineItem(4L, 2L, 4L, 1)));
        List<Order> orders = Arrays.asList(order1, order2);

        given(orderService.list()).willReturn(orders);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(orders)));
    }

    @DisplayName("주문의 주문 상태를 변경하고 주문과 주문 항목을 반환한다.")
    @Test
    void changeOrderStatus() throws Exception {
        // given
        Order order = new Order(order1.getId(), order1.getOrderTableId(), OrderStatus.MEAL.name(), order1.getOrderedTime(), order1.getOrderLineItems());

        given(orderService.changeOrderStatus(order1.getId(), order)).willReturn(order);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(URL + String.format("/%d/order-status", order1.getId()))
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(order)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(order)));
    }
}
