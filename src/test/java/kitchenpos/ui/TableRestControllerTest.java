package kitchenpos.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.annotation.MockMvcEncodingConfiguration;
import kitchenpos.application.TableService;
import kitchenpos.domain.OrderTable;
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

    @BeforeEach
    void setUp() {
        orderTable1 = new OrderTable(1L, null, 0, true);
    }

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() throws Exception {
        // given
        OrderTable orderTable = new OrderTable(orderTable1.getNumberOfGuests(), orderTable1.isEmpty());

        given(tableService.create(orderTable)).willReturn(orderTable1);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(orderTable)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(orderTable1)));
    }

    @DisplayName("주문 테이블 전체 목록을 조회한다.")
    @Test
    void list() throws Exception {
        // given
        OrderTable orderTable2 = new OrderTable(2L, null, 2, true);
        List<OrderTable> orderTables = Arrays.asList(orderTable1, orderTable2);

        given(tableService.list()).willReturn(orderTables);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(orderTables)));
    }

    @DisplayName("주문 테이블을 빈 테이블로 변경하고 변경한 주문 테이블을 반환한다.")
    @Test
    void changeEmpty() throws Exception {
        // given
        orderTable1.setEmpty(false);
        OrderTable orderTable = new OrderTable(orderTable1.getId(), orderTable1.getTableGroupId(), orderTable1.getNumberOfGuests(), true);

        given(tableService.changeEmpty(orderTable1.getId(), orderTable)).willReturn(orderTable);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(URL + String.format("/%d/empty", orderTable1.getId()))
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(orderTable)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(orderTable)));
    }

    @DisplayName("주문 테이블에 방문한 손님 수를 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void changeNumberOfGuests() throws Exception {
        // given
        int numberOfGuests = 1;
        orderTable1.setEmpty(false);
        OrderTable orderTable = new OrderTable(orderTable1.getId(), orderTable1.getTableGroupId(), numberOfGuests, orderTable1.isEmpty());

        given(tableService.changeNumberOfGuests(orderTable1.getId(), orderTable)).willReturn(orderTable);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(URL + String.format("/%d/number-of-guests", orderTable1.getId()))
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(orderTable)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(orderTable)));
    }
}
