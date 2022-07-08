package kitchenpos.table.application;

import kitchenpos.table.domain.*;
import kitchenpos.table.dto.OrderTableChangeEmptyRequest;
import kitchenpos.table.dto.OrderTableChangeNumberOfGuestRequest;
import kitchenpos.table.dto.OrderTableRequest;
import kitchenpos.table.dto.OrderTableResponse;
import kitchenpos.table.exception.GroupedOrderTableException;
import kitchenpos.table.exception.OrderNotCompletionException;
import kitchenpos.table.exception.OrderTableEmptyException;
import kitchenpos.table.exception.OrderTableNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {
    @Mock
    private OrderTableRepository orderTableRepository;

    @Mock
    private OrderTableValidator orderTableValidator;

    @Mock
    private TableEventPublisher tableEventPublisher;

    @InjectMocks
    private TableService tableService;

    private OrderTable orderTable1;
    private OrderTable orderTable2;

    @BeforeEach
    void setUp() {
        orderTable1 = OrderTable.of(1L, null, 0, true);
        orderTable2 = OrderTable.of(2L, null, 0, true);
    }

    @DisplayName("주문 테이블을 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void create() {
        // given
        OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable1.getNumberOfGuests(),
                                                                    orderTable1.isEmpty());

        given(orderTableRepository.save(orderTableRequest.toOrderTable())).willReturn(orderTable1);

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
        given(orderTableRepository.findAll()).willReturn(Arrays.asList(orderTable1, orderTable2));

        // when
        List<OrderTableResponse> orderTableResponses = tableService.list();

        // then
        assertThat(orderTableResponses).extracting("id")
                .containsExactly(orderTable1.getId(), orderTable2.getId());
        assertThat(orderTableResponses).extracting("tableGroupId")
                .containsExactly(null, null);
        assertThat(orderTableResponses).extracting("numberOfGuests")
                .containsExactly(orderTable1.getNumberOfGuests(), orderTable2.getNumberOfGuests());
        assertThat(orderTableResponses).extracting("empty")
                .containsExactly(orderTable1.isEmpty(), orderTable2.isEmpty());
    }

    @DisplayName("주문 테이블의 빈 테이블 여부를 변경하고 변경한 주문 테이블을 반환한다.")
    @Test
    void changeEmpty() {
        // given
        OrderTableChangeEmptyRequest orderTableChangeEmptyRequest = new OrderTableChangeEmptyRequest(false);

        given(orderTableRepository.findById(orderTable1.getId())).willReturn(Optional.of(orderTable1));
        given(orderTableRepository.save(orderTable1)).willReturn(orderTable1);
        given(tableEventPublisher.sendOrderTableEmptyChangeMessage(orderTable1)).willReturn(true);

        // when
        OrderTableResponse orderTableResponse =
                tableService.changeEmpty(orderTable1.getId(), orderTableChangeEmptyRequest);

        // then
        assertThat(orderTableResponse.isEmpty()).isFalse();
    }

    @DisplayName("단체 지정된 주문 테이블은 빈 테이블 여부를 변경할 수 없다.")
    @Test
    void cannotChangeEmptyTableGroup() {
        // given
        OrderTableChangeEmptyRequest orderTableChangeEmptyRequest = new OrderTableChangeEmptyRequest(true);
        OrderTable orderTable = OrderTable.of(1L, new TableGroup(), 1, false);

        given(orderTableRepository.findById(orderTable.getId())).willReturn(Optional.of(orderTable));
        given(orderTableValidator.changeEmpty(any())).willThrow(GroupedOrderTableException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () ->
                tableService.changeEmpty(orderTable.getId(), orderTableChangeEmptyRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(GroupedOrderTableException.class);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 변경할 수 없다.")
    @Test
    void cannotChangeEmptyCookingOrMeal() {
        // given
        OrderTableChangeEmptyRequest orderTableChangeEmptyRequest = new OrderTableChangeEmptyRequest(true);
        OrderTable orderTable =
                OrderTable.of(orderTable1.getId(), orderTable1.getTableGroup(),
                              orderTable1.getNumberOfGuests(), false);
        OrderTable mockOrderTable = mock(orderTable.getClass());

        given(orderTableRepository.findById(orderTable.getId())).willReturn(Optional.of(mockOrderTable));
        given(mockOrderTable.changeEmpty(orderTableValidator, tableEventPublisher, true)).willThrow(OrderNotCompletionException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> tableService.changeEmpty(orderTable1.getId(),
                                                                                           orderTableChangeEmptyRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderNotCompletionException.class);

        then(orderTableRepository).should(times(1)).findById(orderTable.getId());
        then(mockOrderTable).should(times(1)).changeEmpty(orderTableValidator, tableEventPublisher, true);
    }

    @DisplayName("주문 테이블에 방문한 손님 수를 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void changeNumberOfGuests() {
        // given
        OrderTableChangeNumberOfGuestRequest orderTableChangeNumberOfGuestRequest =
                new OrderTableChangeNumberOfGuestRequest(1);
        OrderTable orderTable = OrderTable.of(orderTable1.getId(), orderTable1.getTableGroup(),
                                              orderTable1.getNumberOfGuests(), false);

        given(orderTableRepository.findById(orderTable.getId())).willReturn(Optional.of(orderTable));

        // when
        OrderTableResponse orderTableResponse = tableService.changeNumberOfGuests(orderTable.getId(),
                                                                                  orderTableChangeNumberOfGuestRequest);

        // then
        assertThat(orderTableResponse.getNumberOfGuests()).isEqualTo(
                orderTableChangeNumberOfGuestRequest.getNumberOfGuests());
    }

    @DisplayName("방문한 손님 수는 0명 이상만 가능하다.")
    @Test
    void invalidNumberOfGuests() {
        // given
        OrderTableChangeNumberOfGuestRequest orderTableChangeNumberOfGuestRequest =
                new OrderTableChangeNumberOfGuestRequest(0);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () ->
                tableService.changeNumberOfGuests(orderTable1.getId(), orderTableChangeNumberOfGuestRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableNotFoundException.class)
                .hasMessageContaining(OrderTableNotFoundException.MESSAGE);
    }

    @DisplayName("빈 테이블은 방문한 손님 수를 등록할 수 없다.")
    @Test
    void cannotChangeNumberOfGuestsEmptyTable() {
        // given
        OrderTableChangeNumberOfGuestRequest orderTableChangeNumberOfGuestRequest = new OrderTableChangeNumberOfGuestRequest(
                1);

        given(orderTableRepository.findById(orderTable1.getId())).willReturn(Optional.of(orderTable1));
        given(orderTableValidator.changeNumberOfGuests(any())).willThrow(OrderTableEmptyException.class);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () ->
                tableService.changeNumberOfGuests(orderTable1.getId(), orderTableChangeNumberOfGuestRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableEmptyException.class);
    }
}
