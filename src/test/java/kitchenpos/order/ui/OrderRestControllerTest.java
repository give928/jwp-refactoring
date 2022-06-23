package kitchenpos.order.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.annotation.MockMvcEncodingConfiguration;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuProducts;
import kitchenpos.order.application.OrderService;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderLineItems;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.order.dto.OrderStatusChangeRequest;
import kitchenpos.table.domain.OrderTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
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

    private OrderLineItem orderLineItem1;
    private OrderLineItem orderLineItem2;
    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        Menu menu1 = Menu.of(1L, "메뉴1", BigDecimal.ZERO, null, MenuProducts.from(Collections.emptyList()));
        Menu menu2 = Menu.of(2L, "메뉴2", BigDecimal.ZERO, null, MenuProducts.from(Collections.emptyList()));

        Long orderId1 = 1L;
        OrderTable orderTable1 = OrderTable.of(1L, null, 1, false);
        orderLineItem1 = OrderLineItem.of(1L, null, menu1, 1);
        orderLineItem2 = OrderLineItem.of(2L, null, menu2, 1);
        order1 = Order.of(orderId1, orderTable1, OrderLineItems.from(Arrays.asList(orderLineItem1, orderLineItem2)));

        Long orderId2 = 2L;
        OrderTable orderTable2 = OrderTable.of(2L, null, 1, false);
        order2 = Order.of(orderId2, orderTable2, OrderLineItems.from(Arrays.asList(OrderLineItem.of(3L, null, menu1, 2),
                          OrderLineItem.of(4L, null, menu2, 2))));
    }

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() throws Exception {
        // given
        OrderRequest orderRequest = new OrderRequest(order1.getOrderTable().getId(),
                                                     Arrays.asList(new OrderLineItemRequest(orderLineItem1.getMenu().getId(), orderLineItem1.getQuantity()),
                                                                   new OrderLineItemRequest(orderLineItem2.getMenu().getId(), orderLineItem2.getQuantity())));
        OrderResponse orderResponse = OrderResponse.from(order1);

        given(orderService.create(any())).willReturn(orderResponse);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(orderRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(orderResponse)));
    }

    @DisplayName("주문과 주문 항목의 전체 목록을 조회한다.")
    @Test
    void list() throws Exception {
        // given
        List<OrderResponse> orders = Arrays.asList(OrderResponse.from(order1), OrderResponse.from(order2));

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
        OrderStatus nextOrderStatus = OrderStatus.MEAL;
        OrderStatusChangeRequest orderStatusChangeRequest = new OrderStatusChangeRequest(nextOrderStatus.name());
        order1.changeOrderStatus(nextOrderStatus);
        OrderResponse orderResponse = OrderResponse.from(order1);

        given(orderService.changeOrderStatus(ArgumentMatchers.eq(order1.getId()),
                                             ArgumentMatchers.argThat(argument -> argument != null && Objects.equals(argument.getOrderStatus(), nextOrderStatus.name()))))
                .willReturn(orderResponse);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(URL + String.format("/%d/order-status", order1.getId()))
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(orderStatusChangeRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(orderResponse)));
    }
}
