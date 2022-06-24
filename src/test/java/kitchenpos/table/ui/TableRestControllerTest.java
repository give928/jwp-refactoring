package kitchenpos.table.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.annotation.MockMvcEncodingConfiguration;
import kitchenpos.table.application.TableService;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.dto.OrderTableChangeEmptyRequest;
import kitchenpos.table.dto.OrderTableChangeNumberOfGuestRequest;
import kitchenpos.table.dto.OrderTableRequest;
import kitchenpos.table.dto.OrderTableResponse;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcEncodingConfiguration
@WebMvcTest(TableRestController.class)
class TableRestControllerTest {
    private static final String URL = "/api/tables";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TableService tableService;

    private OrderTable orderTable1;
    private OrderTable orderTable2;

    @BeforeEach
    void setUp() {
        orderTable1 = OrderTable.of(1L, null, 0, true);
        orderTable2 = OrderTable.of(2L, null, 0, true);
    }

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() throws Exception {
        // given
        OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable1.getNumberOfGuests(),
                                                                    orderTable1.isEmpty());
        OrderTableResponse orderTableResponse = OrderTableResponse.from(orderTable1);

        given(tableService.create(any())).willReturn(orderTableResponse);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(
                                                                      orderTableRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(orderTableResponse)));
    }

    @DisplayName("주문 테이블 전체 목록을 조회한다.")
    @Test
    void list() throws Exception {
        // given
        List<OrderTableResponse> orderTableResponses = Arrays.asList(OrderTableResponse.from(orderTable1),
                                                                     OrderTableResponse.from(orderTable2));

        given(tableService.list()).willReturn(orderTableResponses);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(orderTableResponses)));
    }

    @DisplayName("주문 테이블의 빈 테이블 여부를 변경하고 변경한 주문 테이블을 반환한다.")
    @Test
    void changeEmpty() throws Exception {
        // given
        OrderTable orderTable = OrderTable.of(orderTable1.getId(), orderTable1.getTableGroup(),
                                              orderTable1.getNumberOfGuests(), true);
        OrderTableChangeEmptyRequest orderTableChangeEmptyRequest = new OrderTableChangeEmptyRequest(!orderTable.isEmpty());
        OrderTableResponse orderTableResponse = OrderTableResponse.from(orderTable);

        given(tableService.changeEmpty(eq(orderTable1.getId()), any())).willReturn(orderTableResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.put(URL + String.format("/%d/empty", orderTable1.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(orderTableChangeEmptyRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(orderTableResponse)));
    }

    @DisplayName("주문 테이블에 방문한 손님 수를 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void changeNumberOfGuests() throws Exception {
        // given
        OrderTableChangeNumberOfGuestRequest orderTableChangeNumberOfGuestRequest =
                new OrderTableChangeNumberOfGuestRequest(1);
        OrderTableResponse orderTableResponse = OrderTableResponse.from(
                OrderTable.of(orderTable1.getId(), orderTable1.getTableGroup(),
                              orderTableChangeNumberOfGuestRequest.getNumberOfGuests(), orderTable1.isEmpty()));

        given(tableService.changeNumberOfGuests(eq(orderTable1.getId()), any())).willReturn(orderTableResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                        MockMvcRequestBuilders.put(URL + String.format("/%d/number-of-guests", orderTable1.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(orderTableChangeNumberOfGuestRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(orderTableResponse)));
    }
}
