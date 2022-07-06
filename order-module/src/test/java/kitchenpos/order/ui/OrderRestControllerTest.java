package kitchenpos.order.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.common.annotation.MockMvcEncodingConfiguration;
import kitchenpos.order.application.OrderService;
import kitchenpos.order.dto.*;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private OrderResponse orderResponse1;
    private OrderResponse orderResponse2;

    @BeforeEach
    void setUp() {
        Long orderId1 = 1L;
        orderResponse1 = new OrderResponse(orderId1, 1L, "COOKING", LocalDateTime.now(),
                                           Arrays.asList(new OrderLineItemResponse(1L, orderId1, 1L, 1),
                                                         new OrderLineItemResponse(2L, orderId1, 2L, 1)));
        Long orderId2 = 2L;
        orderResponse2 = new OrderResponse(orderId2, 2L, "COOKING", LocalDateTime.now(),
                                           Arrays.asList(new OrderLineItemResponse(3L, orderId2, 3L, 1),
                                                         new OrderLineItemResponse(4L, orderId2, 4L, 1)));
    }

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() throws Exception {
        // given
        OrderRequest orderRequest = createOrderRequestBy(orderResponse1);

        given(orderService.create(any())).willReturn(orderResponse1);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(orderRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(orderResponse1)));
    }

    @DisplayName("주문과 주문 항목의 전체 목록을 조회한다.")
    @Test
    void list() throws Exception {
        // given
        List<OrderResponse> orders = Arrays.asList(orderResponse1, orderResponse2);

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
        String orderStatus = "MEAL";
        OrderStatusChangeRequest orderStatusChangeRequest = new OrderStatusChangeRequest(orderStatus);
        OrderResponse orderResponse = new OrderResponse(orderResponse1.getId(), orderResponse1.getOrderTableId(),
                                                        orderStatus, orderResponse1.getOrderedTime(),
                                                        orderResponse1.getOrderLineItems());

        given(orderService.changeOrderStatus(ArgumentMatchers.eq(orderResponse1.getId()),
                                             ArgumentMatchers.argThat(argument -> argument != null && Objects.equals(
                                                     argument.getOrderStatus(), orderStatus))))
                .willReturn(orderResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.put(URL + String.format("/%d/order-status", orderResponse.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(orderStatusChangeRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(orderResponse)));
    }

    private OrderRequest createOrderRequestBy(OrderResponse orderResponse) {
        return new OrderRequest(orderResponse.getOrderTableId(),
                                getOrderLineItemRequests(orderResponse));
    }

    private List<OrderLineItemRequest> getOrderLineItemRequests(OrderResponse orderResponse) {
        return orderResponse.getOrderLineItems().stream()
                .map(orderLineItem -> new OrderLineItemRequest(orderLineItem.getMenuId(),
                                                               orderLineItem.getQuantity()))
                .collect(Collectors.toList());
    }
}
