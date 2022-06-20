package kitchenpos.table.application;

import kitchenpos.order.dao.OrderDao;
import kitchenpos.table.dao.OrderTableDao;
import kitchenpos.table.dao.TableGroupDao;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.TableGroup;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TableGroupServiceTest {
    @Mock
    private OrderDao orderDao;
    @Mock
    private OrderTableDao orderTableDao;
    @Mock
    private TableGroupDao tableGroupDao;

    @InjectMocks
    private TableGroupService tableGroupService;

    private OrderTable orderTable1;
    private OrderTable orderTable2;
    private TableGroup tableGroup1;

    public static Stream<List<OrderTableGroupRequest>> invalidOrderTablesParameter() {
        return Stream.of(null, Collections.emptyList(), Collections.singletonList(new OrderTableGroupRequest(1L)));
    }

    public static Stream<List<OrderTable>> notEmptyOrNotNullTableGroupIdOfOrderTableParameter() {
        return Stream.of(Arrays.asList(new OrderTable(1L, null, 1, false),
                                       new OrderTable(2L, null, 1, true)),
                         Arrays.asList(new OrderTable(1L, 1L, 1, true),
                                       new OrderTable(2L, null, 1, true)));
    }

    @BeforeEach
    void setUp() {
        Long tableGroupId1 = 1L;
        orderTable1 = new OrderTable(1L, tableGroupId1, 2, false);
        orderTable2 = new OrderTable(2L, tableGroupId1, 4, false);
        tableGroup1 = new TableGroup(tableGroupId1, LocalDateTime.now(),
                                     Stream.of(orderTable1, orderTable2).collect(Collectors.toList()));
    }

    @DisplayName("주문 테이블을 단체 지정을 등록하고 등록한 단체 지정을 반환한다.")
    @Test
    void create() {
        // given
        OrderTable orderTable1 = new OrderTable(1L, null, 0, true);
        OrderTable orderTable2 = new OrderTable(2L, null, 0, true);
        TableGroup tableGroup = new TableGroup(tableGroup1.getId(), tableGroup1.getCreatedDate());
        TableGroupRequest tableGroupRequest = new TableGroupRequest(Arrays.asList(new OrderTableGroupRequest(orderTable1.getId()),
                                                                                  new OrderTableGroupRequest(orderTable2.getId())));

        given(orderTableDao.findAllByIdIn(tableGroupRequest.getOrderTables().stream()
                                                  .mapToLong(OrderTableGroupRequest::getId)
                                                  .boxed()
                                                  .collect(Collectors.toList()))).willReturn(Arrays.asList(orderTable1, orderTable2));
        given(tableGroupDao.save(any())).willReturn(tableGroup);
        given(orderTableDao.save(argThat(argument -> argument != null && Objects.equals(argument.getId(), orderTable1.getId()))))
                .willReturn(new OrderTable(orderTable1.getId(), tableGroup.getId(), orderTable1.getNumberOfGuests(), false));
        given(orderTableDao.save(argThat(argument -> argument != null && Objects.equals(argument.getId(), orderTable2.getId()))))
                .willReturn(new OrderTable(orderTable2.getId(), tableGroup.getId(), orderTable2.getNumberOfGuests(), false));

        // when
        TableGroupResponse tableGroupResponse = tableGroupService.create(tableGroupRequest);

        // then
        assertThat(tableGroupResponse.getOrderTables()).extracting("tableGroupId")
                .containsExactly(tableGroup.getId(), tableGroup.getId());
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

        given(orderTableDao.findAllByIdIn(any())).willReturn(Collections.emptyList());

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
        TableGroupRequest tableGroupRequest = new TableGroupRequest(Arrays.asList(new OrderTableGroupRequest(orderTable1.getId()),
                                                                                  new OrderTableGroupRequest(orderTable2.getId())));

        given(orderTableDao.findAllByIdIn(any())).willReturn(savedOrderTables);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.create(tableGroupRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void ungroup() {
        // given
        given(orderTableDao.findAllByTableGroupId(tableGroup1.getId())).willReturn(tableGroup1.getOrderTables());
        given(orderDao.existsByOrderTableIdInAndOrderStatusIn(
                tableGroup1.getOrderTables().stream()
                        .mapToLong(OrderTable::getId)
                        .boxed()
                        .collect(Collectors.toList()),
                Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()))).willReturn(false);

        // when
        tableGroupService.ungroup(tableGroup1.getId());

        // then
        assertThat(tableGroup1.getOrderTables()).extracting("tableGroupId").containsExactly(null, null);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 변경할 수 없다.")
    @Test
    void invalidOrderStatus() {
        // given
        given(orderTableDao.findAllByTableGroupId(tableGroup1.getId())).willReturn(tableGroup1.getOrderTables());
        given(orderDao.existsByOrderTableIdInAndOrderStatusIn(
                tableGroup1.getOrderTables().stream()
                        .mapToLong(OrderTable::getId)
                        .boxed()
                        .collect(Collectors.toList()),
                Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()))).willReturn(true);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.ungroup(tableGroup1.getId());

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
}
