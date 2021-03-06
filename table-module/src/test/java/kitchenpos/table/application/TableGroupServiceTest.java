package kitchenpos.table.application;

import kitchenpos.table.domain.*;
import kitchenpos.table.dto.OrderTableGroupRequest;
import kitchenpos.table.dto.OrderTableResponse;
import kitchenpos.table.dto.TableGroupRequest;
import kitchenpos.table.dto.TableGroupResponse;
import kitchenpos.table.exception.OrderTableEmptyException;
import kitchenpos.table.exception.OrderTableNotFoundException;
import kitchenpos.table.exception.RequiredOrderTablesOfTableGroupException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kitchenpos.table.TableFixtures.aTableGroup1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TableGroupServiceTest {
    @Mock
    private OrderTableRepository orderTableRepository;
    @Mock
    private TableGroupRepository tableGroupRepository;
    @Mock
    private TableGroupValidator tableGroupValidator;

    @InjectMocks
    private TableGroupService tableGroupService;

    private OrderTable orderTable1;
    private OrderTable orderTable2;
    private TableGroup tableGroup1;

    public static Stream<List<OrderTableGroupRequest>> invalidOrderTablesParameter() {
        return Stream.of(null, Collections.emptyList(), Collections.singletonList(new OrderTableGroupRequest(1L)));
    }

    public static Stream<List<OrderTable>> notEmptyOrNotNullTableGroupIdOfOrderTableParameter() {
        return Stream.of(Arrays.asList(OrderTable.of(1L, null, 1, false),
                                       OrderTable.of(2L, null, 1, true)),
                         Arrays.asList(OrderTable.of(1L, new TableGroup(), 1, true),
                                       OrderTable.of(2L, null, 1, true)));
    }

    @BeforeEach
    void setUp() {
        tableGroup1 = aTableGroup1();
        orderTable1 = tableGroup1.getOrderTables().get(0);
        orderTable2 = tableGroup1.getOrderTables().get(1);
    }

    @DisplayName("?????? ???????????? ?????? ????????? ???????????? ????????? ?????? ????????? ????????????.")
    @Test
    void create() {
        // given
        TableGroupRequest tableGroupRequest = createTableGroupRequest(orderTable1, orderTable2);

        given(orderTableRepository.findAllByIdIn(tableGroupRequest.getOrderTables().stream()
                                                         .mapToLong(OrderTableGroupRequest::getId)
                                                         .boxed()
                                                         .collect(Collectors.toList())))
                .willReturn(Arrays.asList(OrderTable.of(1L, null, 0, true),
                                          OrderTable.of(2L, null, 0, true)));
        given(tableGroupRepository.save(any())).willReturn(tableGroup1);

        // when
        TableGroupResponse tableGroupResponse = tableGroupService.create(tableGroupRequest);

        // then
        assertThat(tableGroupResponse.getOrderTables()).extracting("tableGroupId")
                .containsExactly(tableGroup1.getId(), tableGroup1.getId());
        assertThat(tableGroupResponse.getOrderTables()).extracting("empty")
                .containsExactly(false, false);
    }

    @DisplayName("?????? ???????????? 2??? ????????? ????????? ????????? ??? ??????.")
    @ParameterizedTest
    @MethodSource("invalidOrderTablesParameter")
    void invalidOrderTables(List<OrderTableGroupRequest> orderTables) {
        // given
        List<Long> orderTableIds = Optional.ofNullable(orderTables)
                .orElse(Collections.emptyList())
                .stream()
                .map(OrderTableGroupRequest::getId)
                .collect(Collectors.toList());
        TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTables);

        given(tableGroupValidator.validateIfLessOrderTables(orderTableIds)).willThrow(
                RequiredOrderTablesOfTableGroupException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.create(tableGroupRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredOrderTablesOfTableGroupException.class);
    }

    @DisplayName("????????? ?????? ???????????? ????????? ????????? ??? ??????.")
    @Test
    void notExistsOrderTables() {
        // given
        List<Long> orderTableIds = Arrays.asList(-1L, -2L);
        TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTableIds.stream()
                                                                            .map(OrderTableGroupRequest::new)
                                                                            .collect(Collectors.toList()));

        given(orderTableRepository.findAllByIdIn(orderTableIds)).willReturn(Collections.emptyList());
        given(tableGroupValidator.validateIfNotFoundOrderTables(orderTableIds, Collections.emptyList())).willThrow(
                OrderTableNotFoundException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.create(tableGroupRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableNotFoundException.class);
    }

    @DisplayName("?????? ???????????? ???????????? ?????????, ?????? ?????? ????????? ?????? ????????? ????????? ??? ??????.")
    @ParameterizedTest
    @MethodSource("notEmptyOrNotNullTableGroupIdOfOrderTableParameter")
    void notEmptyOrNotNullTableGroupIdOfOrderTable(List<OrderTable> savedOrderTables) {
        // given
        TableGroupRequest tableGroupRequest = createTableGroupRequest(orderTable1, orderTable2);

        given(orderTableRepository.findAllByIdIn(any())).willReturn(savedOrderTables);
        given(tableGroupValidator.create(any())).willThrow(OrderTableEmptyException.throwBy(false));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.create(tableGroupRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableEmptyException.class)
                .hasMessageContaining(OrderTableEmptyException.NOT_EMPTY_MESSAGE);
    }

    @DisplayName("?????? ????????? ????????????.")
    @Test
    void ungroup() {
        // given
        given(tableGroupRepository.findById(tableGroup1.getId())).willReturn(Optional.of(tableGroup1));

        // when
        tableGroupService.ungroup(tableGroup1.getId());

        // then
        assertThat(tableGroup1.getOrderTables()).isEmpty();
    }

    @DisplayName("?????? ????????? ????????????.")
    @Test
    void find() {
        // given
        given(tableGroupRepository.findById(tableGroup1.getId())).willReturn(Optional.of(tableGroup1));

        // when
        TableGroupResponse tableGroupResponse = tableGroupService.find(tableGroup1.getId());

        // then
        assertThat(tableGroupResponse.getId()).isEqualTo(tableGroup1.getId());
        assertThat(tableGroupResponse.getCreatedDate()).isEqualTo(tableGroup1.getCreatedDate());
        assertThat(tableGroupResponse.getOrderTables()
                           .stream()
                           .map(OrderTableResponse::getId)
                           .sorted()
                           .collect(Collectors.toList()))
                .isEqualTo(tableGroup1.getOrderTables()
                                   .stream()
                                   .map(OrderTable::getId)
                                   .sorted()
                                   .collect(Collectors.toList()));
    }

    private TableGroupRequest createTableGroupRequest(OrderTable... orderTables) {
        return new TableGroupRequest(Stream.of(orderTables)
                                             .map(orderTable -> new OrderTableGroupRequest(orderTable.getId()))
                                             .collect(Collectors.toList()));
    }
}
