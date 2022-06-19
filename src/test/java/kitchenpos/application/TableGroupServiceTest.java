package kitchenpos.application;

import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    private TableGroup tableGroup1;

    public static Stream<List<OrderTable>> invalidOrderTablesParameter() {
        return Stream.of(null, Collections.emptyList(), Collections.singletonList(new OrderTable(1L, null, 1, false)));
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
        OrderTable orderTable1 = new OrderTable(1L, tableGroupId1, 1, false);
        OrderTable orderTable2 = new OrderTable(2L, tableGroupId1, 2, false);
        tableGroup1 = new TableGroup(tableGroupId1, LocalDateTime.now(), Arrays.asList(orderTable1, orderTable2));
    }

    @DisplayName("주문 테이블을 단체 지정을 등록하고 등록한 단체 지정을 반환한다.")
    @Test
    void create() {
        // given
        List<OrderTable> orderTables = Arrays.asList(new OrderTable(1L, null, 1, true),
                                                     new OrderTable(2L, null, 2, true));
        TableGroup tableGroup = new TableGroup(tableGroup1.getCreatedDate(), orderTables);

        given(orderTableDao.findAllByIdIn(tableGroup1.getOrderTables().stream()
                                                  .mapToLong(OrderTable::getId)
                                                  .boxed()
                                                  .collect(Collectors.toList()))).willReturn(orderTables);
        given(tableGroupDao.save(tableGroup)).willReturn(tableGroup1);
        given(orderTableDao.save(any())).willReturn(any());

        // when
        TableGroup savedTableGroup = tableGroupService.create(tableGroup);

        // then
        assertThat(savedTableGroup).isEqualTo(tableGroup1);
        assertThat(savedTableGroup.getOrderTables()).extracting("tableGroupId").containsExactly(tableGroup1.getId(),
                                                                                                tableGroup1.getId());
        assertThat(savedTableGroup.getOrderTables()).extracting("empty").containsExactly(false, false);
    }

    @DisplayName("주문 테이블은 2개 이상만 단체로 지정할 수 있다.")
    @ParameterizedTest
    @MethodSource("invalidOrderTablesParameter")
    void invalidOrderTables(List<OrderTable> orderTables) {
        // given
        TableGroup tableGroup = new TableGroup(tableGroup1.getCreatedDate(), orderTables);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.create(tableGroup);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("등록된 주문 테이블만 단체로 지정할 수 있다.")
    @Test
    void notExistsOrderTables() {
        // given
        List<OrderTable> orderTables = Arrays.asList(new OrderTable(-1L, null, 1, true),
                                                     new OrderTable(-2L, null, 2, true));
        TableGroup tableGroup = new TableGroup(tableGroup1.getCreatedDate(), orderTables);

        given(orderTableDao.findAllByIdIn(any())).willReturn(Collections.emptyList());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.create(tableGroup);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 테이블이 비어있지 않거나, 이미 단체 지정이 되어 있으면 등록할 수 없다.")
    @ParameterizedTest
    @MethodSource("notEmptyOrNotNullTableGroupIdOfOrderTableParameter")
    void notEmptyOrNotNullTableGroupIdOfOrderTable(List<OrderTable> savedOrderTables) {
        // given
        List<OrderTable> orderTables = Arrays.asList(new OrderTable(1L, null, 1, true),
                                                     new OrderTable(2L, null, 2, true));
        TableGroup tableGroup = new TableGroup(tableGroup1.getCreatedDate(), orderTables);

        given(orderTableDao.findAllByIdIn(any())).willReturn(savedOrderTables);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableGroupService.create(tableGroup);

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
