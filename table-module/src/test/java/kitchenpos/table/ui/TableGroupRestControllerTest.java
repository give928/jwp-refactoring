package kitchenpos.table.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.common.annotation.MockMvcEncodingConfiguration;
import kitchenpos.table.application.TableGroupService;
import kitchenpos.table.dto.OrderTableGroupRequest;
import kitchenpos.table.dto.OrderTableResponse;
import kitchenpos.table.dto.TableGroupRequest;
import kitchenpos.table.dto.TableGroupResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcEncodingConfiguration
@WebMvcTest(TableGroupRestController.class)
class TableGroupRestControllerTest {
    private static final String URL = "/api/table-groups";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TableGroupService tableGroupService;

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() throws Exception {
        // given
        TableGroupRequest tableGroupRequest = new TableGroupRequest(Arrays.asList(new OrderTableGroupRequest(1L),
                                                                                  new OrderTableGroupRequest(2L)));
        TableGroupResponse tableGroupResponse =
                new TableGroupResponse(1L, LocalDateTime.now(),
                                       tableGroupRequest.getOrderTables().stream()
                                               .map(orderTableGroupRequest -> new OrderTableResponse(orderTableGroupRequest.getId(),
                                                       1L, 1, false))
                                               .collect(Collectors.toList()));

        given(tableGroupService.create(any())).willReturn(tableGroupResponse);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(
                                                                      tableGroupRequest)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(tableGroupResponse)));
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void ungroup() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.delete(URL + String.format("/%d", 1L)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNoContent());
    }
}
