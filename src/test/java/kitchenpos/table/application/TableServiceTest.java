package kitchenpos.table.application;

import kitchenpos.order.dao.OrderDao;
import kitchenpos.table.application.TableService;
import kitchenpos.table.dao.OrderTableDao;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.table.domain.OrderTable;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {
    @Mock
    private OrderDao orderDao;
    @Mock
    private OrderTableDao orderTableDao;

    @InjectMocks
    private TableService tableService;

    private OrderTable savedOrderTable;

    @BeforeEach
    void setUp() {
        savedOrderTable = new OrderTable(1L, null, 0, true);
    }

    @DisplayName("주문 테이블을 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void create() {
        // given
        OrderTable orderTable = new OrderTable(savedOrderTable.getNumberOfGuests(), savedOrderTable.isEmpty());

        given(orderTableDao.save(orderTable)).willReturn(savedOrderTable);

        // when
        OrderTable savedOrderTable = tableService.create(orderTable);

        // then
        assertThat(savedOrderTable).isEqualTo(this.savedOrderTable);
    }

    @DisplayName("주문 테이블 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        OrderTable orderTable2 = new OrderTable(2L, null, 2, true);
        List<OrderTable> orderTables = Arrays.asList(savedOrderTable, orderTable2);

        given(orderTableDao.findAll()).willReturn(orderTables);

        // when
        List<OrderTable> findOrderTables = tableService.list();

        // then
        assertThat(findOrderTables).containsExactlyElementsOf(orderTables);
    }

    @DisplayName("주문 테이블을 빈 테이블로 변경하고 변경한 주문 테이블을 반환한다.")
    @Test
    void changeEmpty() {
        // given
        savedOrderTable.setEmpty(false);
        OrderTable orderTable = new OrderTable(savedOrderTable.getId(), savedOrderTable.getTableGroupId(),
                                               savedOrderTable.getNumberOfGuests(), true);

        given(orderTableDao.findById(orderTable.getId())).willReturn(Optional.of(savedOrderTable));
        given(orderDao.existsByOrderTableIdAndOrderStatusIn(savedOrderTable.getId(),
                                                            Arrays.asList(OrderStatus.COOKING.name(),
                                                                          OrderStatus.MEAL.name())))
                .willReturn(false);
        given(orderTableDao.save(savedOrderTable)).willReturn(orderTable);

        // when
        OrderTable changedOrderTable = tableService.changeEmpty(savedOrderTable.getId(), orderTable);

        // then
        assertThat(changedOrderTable.isEmpty()).isTrue();
    }

    @DisplayName("단체 지정된 주문 테이블은 빈 테이블로 변경할 수 없다.")
    @Test
    void cannotChangeEmptyTableGroup() {
        // given
        OrderTable orderTable = new OrderTable(1L, 1L, 1, false);

        given(orderTableDao.findById(orderTable.getId())).willReturn(Optional.of(orderTable));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableService.changeEmpty(orderTable.getId(),
                                                                                           orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 변경할 수 없다.")
    @Test
    void cannotChangeEmptyCookingOrMeal() {
        // given
        savedOrderTable.setEmpty(false);
        OrderTable orderTable = new OrderTable(savedOrderTable.getId(), savedOrderTable.getTableGroupId(),
                                               savedOrderTable.getNumberOfGuests(), true);

        given(orderTableDao.findById(orderTable.getId())).willReturn(Optional.of(savedOrderTable));
        given(orderDao.existsByOrderTableIdAndOrderStatusIn(savedOrderTable.getId(),
                                                            Arrays.asList(OrderStatus.COOKING.name(),
                                                                          OrderStatus.MEAL.name())))
                .willReturn(true);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableService.changeEmpty(orderTable.getId(),
                                                                                           orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 테이블에 방문한 손님 수를 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void changeNumberOfGuests() {
        // given
        int numberOfGuests = 1;
        savedOrderTable.setEmpty(false);
        OrderTable orderTable = new OrderTable(savedOrderTable.getId(), savedOrderTable.getTableGroupId(),
                                               numberOfGuests, savedOrderTable.isEmpty());

        given(orderTableDao.findById(orderTable.getId())).willReturn(Optional.of(savedOrderTable));
        given(orderTableDao.save(savedOrderTable)).willReturn(orderTable);

        // when
        OrderTable changedOrderTable = tableService.changeNumberOfGuests(savedOrderTable.getId(), orderTable);

        // then
        assertThat(changedOrderTable.getNumberOfGuests()).isEqualTo(numberOfGuests);
    }

    @DisplayName("방문한 손님 수는 0명 이상만 가능하다.")
    @Test
    void invalidNumberOfGuests() {
        // given
        int numberOfGuests = 0;
        OrderTable orderTable = new OrderTable(savedOrderTable.getId(), savedOrderTable.getTableGroupId(),
                                               numberOfGuests, savedOrderTable.isEmpty());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableService.changeNumberOfGuests(orderTable.getId(),
                                                                                                    orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 테이블은 방문한 손님 수를 등록할 수 없다.")
    @Test
    void cannotChangeNumberOfGuestsEmptyTable() {
        // given
        OrderTable orderTable = new OrderTable(savedOrderTable.getId(), savedOrderTable.getTableGroupId(),
                                               1, true);

        given(orderTableDao.findById(orderTable.getId())).willReturn(Optional.of(savedOrderTable));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableService.changeNumberOfGuests(orderTable.getId(),
                                                                                                    orderTable);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
}
