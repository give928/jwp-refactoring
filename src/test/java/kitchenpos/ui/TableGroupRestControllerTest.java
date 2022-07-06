package kitchenpos.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.annotation.MockMvcEncodingConfiguration;
import kitchenpos.application.TableGroupService;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
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
        Long savedTableGroupId = 1L;
        OrderTable savedOrderTable1 = new OrderTable(1L, savedTableGroupId, 1, false);
        OrderTable savedOrderTable2 = new OrderTable(2L, savedTableGroupId, 2, false);
        TableGroup savedTableGroup = new TableGroup(savedTableGroupId, LocalDateTime.now(),
                                                    Arrays.asList(savedOrderTable1, savedOrderTable2));

        TableGroup tableGroup = new TableGroup(savedTableGroup.getCreatedDate(),
                                               Arrays.asList(new OrderTable(1L, null, 1, true),
                                                             new OrderTable(2L, null, 2, true)));

        given(tableGroupService.create(tableGroup)).willReturn(savedTableGroup);

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(objectMapper.writeValueAsBytes(tableGroup)))
                .andDo(print());

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(savedTableGroup)));
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
