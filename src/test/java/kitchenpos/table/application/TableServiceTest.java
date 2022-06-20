package kitchenpos.table.application;

import kitchenpos.order.dao.OrderDao;
import kitchenpos.table.dao.OrderTableDao;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.dto.OrderTableChangeEmptyRequest;
import kitchenpos.table.dto.OrderTableChangeNumberOfGuestRequest;
import kitchenpos.table.dto.OrderTableRequest;
import kitchenpos.table.dto.OrderTableResponse;
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

    private OrderTable orderTable1;
    private OrderTable orderTable2;

    @BeforeEach
    void setUp() {
        orderTable1 = new OrderTable(1L, null, 0, true);
        orderTable2 = new OrderTable(2L, null, 0, true);
    }

    @DisplayName("주문 테이블을 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void create() {
        // given
        OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable1.getNumberOfGuests(), orderTable1.isEmpty());

        given(orderTableDao.save(orderTableRequest.toOrderTable())).willReturn(orderTable1);

        // when
        OrderTableResponse orderTableResponse = tableService.create(orderTableRequest);

        // then
        assertThat(orderTableResponse.getId()).isEqualTo(orderTable1.getId());
        assertThat(orderTableResponse.getTableGroupId()).isNull();
        assertThat(orderTableResponse.getNumberOfGuests()).isEqualTo(orderTable1.getNumberOfGuests());
        assertThat(orderTableResponse.isEmpty()).isEqualTo(orderTable1.isEmpty());
    }

    @DisplayName("주문 테이블 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        given(orderTableDao.findAll()).willReturn(Arrays.asList(orderTable1, orderTable2));

        // when
        List<OrderTableResponse> orderTableResponses = tableService.list();

        // then
        assertThat(orderTableResponses).extracting("id").containsExactly(orderTable1.getId(), orderTable2.getId());
        assertThat(orderTableResponses).extracting("tableGroupId").containsExactly(orderTable1.getTableGroupId(), orderTable2.getTableGroupId());
        assertThat(orderTableResponses).extracting("numberOfGuests").containsExactly(orderTable1.getNumberOfGuests(), orderTable2.getNumberOfGuests());
        assertThat(orderTableResponses).extracting("empty").containsExactly(orderTable1.isEmpty(), orderTable2.isEmpty());
    }

    @DisplayName("주문 테이블의 빈 테이블 여부를 변경하고 변경한 주문 테이블을 반환한다.")
    @Test
    void changeEmpty() {
        // given
        OrderTableChangeEmptyRequest orderTableChangeEmptyRequest = new OrderTableChangeEmptyRequest(false);

        given(orderTableDao.findById(orderTable1.getId())).willReturn(Optional.of(orderTable1));
        given(orderDao.existsByOrderTableIdAndOrderStatusIn(orderTable1.getId(),
                                                            Arrays.asList(OrderStatus.COOKING.name(),
                                                                          OrderStatus.MEAL.name())))
                .willReturn(false);
        given(orderTableDao.save(orderTable1)).willReturn(new OrderTable(orderTable1.getId(), orderTable1.getTableGroupId(),
                                                                         orderTable1.getNumberOfGuests(), orderTableChangeEmptyRequest.isEmpty()));

        // when
        OrderTableResponse orderTableResponse = tableService.changeEmpty(orderTable1.getId(), orderTableChangeEmptyRequest);

        // then
        assertThat(orderTableResponse.isEmpty()).isFalse();
    }

    @DisplayName("단체 지정된 주문 테이블은 빈 테이블 여부를 변경할 수 없다.")
    @Test
    void cannotChangeEmptyTableGroup() {
        // given
        OrderTableChangeEmptyRequest orderTableChangeEmptyRequest = new OrderTableChangeEmptyRequest(false);
        OrderTable orderTable = new OrderTable(1L, 1L, 1, false);

        given(orderTableDao.findById(orderTable.getId())).willReturn(Optional.of(orderTable));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableService.changeEmpty(orderTable.getId(),
                                                                                           orderTableChangeEmptyRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 변경할 수 없다.")
    @Test
    void cannotChangeEmptyCookingOrMeal() {
        // given
        OrderTableChangeEmptyRequest orderTableChangeEmptyRequest = new OrderTableChangeEmptyRequest(false);

        given(orderTableDao.findById(orderTable1.getId())).willReturn(Optional.of(orderTable1));
        given(orderDao.existsByOrderTableIdAndOrderStatusIn(orderTable1.getId(),
                                                            Arrays.asList(OrderStatus.COOKING.name(),
                                                                          OrderStatus.MEAL.name())))
                .willReturn(true);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableService.changeEmpty(orderTable1.getId(),
                                                                                           orderTableChangeEmptyRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 테이블에 방문한 손님 수를 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void changeNumberOfGuests() {
        // given
        OrderTableChangeNumberOfGuestRequest orderTableChangeNumberOfGuestRequest = new OrderTableChangeNumberOfGuestRequest(1);
        OrderTable orderTable = new OrderTable(orderTable1.getId(), orderTable1.getTableGroupId(), orderTable1.getNumberOfGuests(), false);

        given(orderTableDao.findById(orderTable.getId())).willReturn(Optional.of(orderTable));
        given(orderTableDao.save(orderTable))
                .willReturn(new OrderTable(orderTable1.getId(), orderTable1.getTableGroupId(),
                                           orderTableChangeNumberOfGuestRequest.getNumberOfGuests(),
                                           orderTable1.isEmpty()));

        // when
        OrderTableResponse orderTableResponse = tableService.changeNumberOfGuests(orderTable1.getId(), orderTableChangeNumberOfGuestRequest);

        // then
        assertThat(orderTableResponse.getNumberOfGuests()).isEqualTo(orderTableChangeNumberOfGuestRequest.getNumberOfGuests());
    }

    @DisplayName("방문한 손님 수는 0명 이상만 가능하다.")
    @Test
    void invalidNumberOfGuests() {
        // given
        OrderTableChangeNumberOfGuestRequest orderTableChangeNumberOfGuestRequest = new OrderTableChangeNumberOfGuestRequest(0);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableService.changeNumberOfGuests(orderTable1.getId(),
                                                                                                    orderTableChangeNumberOfGuestRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 테이블은 방문한 손님 수를 등록할 수 없다.")
    @Test
    void cannotChangeNumberOfGuestsEmptyTable() {
        // given
        OrderTableChangeNumberOfGuestRequest orderTableChangeNumberOfGuestRequest = new OrderTableChangeNumberOfGuestRequest(1);

        given(orderTableDao.findById(orderTable1.getId())).willReturn(Optional.of(orderTable1));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableService.changeNumberOfGuests(orderTable1.getId(),
                                                                                                    orderTableChangeNumberOfGuestRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
}
