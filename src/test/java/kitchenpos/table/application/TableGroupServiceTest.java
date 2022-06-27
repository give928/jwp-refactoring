package kitchenpos.table.application;

import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.table.domain.*;
import kitchenpos.table.dto.OrderTableGroupRequest;
import kitchenpos.table.dto.TableGroupRequest;
import kitchenpos.table.dto.TableGroupResponse;
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
        orderTable1 = OrderTable.of(1L, null, 0, true);
        orderTable2 = OrderTable.of(2L, null, 0, true);
        tableGroup1 = TableGroup.of(1L, Arrays.asList(orderTable1, orderTable2), tableGroupValidator);
    }

    @DisplayName("주문 테이블을 단체 지정을 등록하고 등록한 단체 지정을 반환한다.")
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

    @DisplayName("주문 테이블은 2개 이상만 단체로 지정할 수 있다.")
    @ParameterizedTest
    @MethodSource("invalidOrderTablesParameter")
    void invalidOrderTables(List<OrderTableGroupRequest> orderTables) {
        // given
        TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTables);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.create(tableGroupRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("등록된 주문 테이블만 단체로 지정할 수 있다.")
    @Test
    void notExistsOrderTables() {
        // given
        TableGroupRequest tableGroupRequest = new TableGroupRequest(Arrays.asList(new OrderTableGroupRequest(-1L),
                                                                                  new OrderTableGroupRequest(-2L)));

        given(orderTableRepository.findAllByIdIn(any())).willReturn(Collections.emptyList());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.create(tableGroupRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 테이블이 비어있지 않거나, 이미 단체 지정이 되어 있으면 등록할 수 없다.")
    @ParameterizedTest
    @MethodSource("notEmptyOrNotNullTableGroupIdOfOrderTableParameter")
    void notEmptyOrNotNullTableGroupIdOfOrderTable(List<OrderTable> savedOrderTables) {
        // given
        TableGroupRequest tableGroupRequest = createTableGroupRequest(orderTable1, orderTable2);

        given(orderTableRepository.findAllByIdIn(any())).willReturn(savedOrderTables);
        given(tableGroupValidator.create(any())).willThrow(IllegalArgumentException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.create(tableGroupRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void ungroup() {
        // given
        given(tableGroupRepository.findById(tableGroup1.getId())).willReturn(Optional.of(tableGroup1));

        // when
        tableGroupService.ungroup(tableGroup1.getId());

        // then
        assertThat(tableGroup1.getOrderTables()).extracting("tableGroup").containsExactly(null, null);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 단체 지정을 해제할 수 없다.")
    @Test
    void invalidOrderStatus() {
        // given
        given(tableGroupRepository.findById(tableGroup1.getId())).willReturn(Optional.of(tableGroup1));
        given(tableGroupValidator.clearTableGroup(any())).willThrow(IllegalArgumentException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.ungroup(tableGroup1.getId());

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    private TableGroupRequest createTableGroupRequest(OrderTable... orderTables) {
        return new TableGroupRequest(Stream.of(orderTables)
                                             .map(orderTable -> new OrderTableGroupRequest(orderTable.getId()))
                                             .collect(Collectors.toList()));
    }
}
