package kitchenpos.table.application;

import kitchenpos.common.domain.Name;
import kitchenpos.common.domain.Price;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuProducts;
import kitchenpos.order.domain.*;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
import kitchenpos.table.domain.TableGroup;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {
    @Mock
    private OrderTableRepository orderTableRepository;

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

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () ->
                tableService.changeEmpty(orderTable.getId(), orderTableChangeEmptyRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 변경할 수 없다.")
    @Test
    void cannotChangeEmptyCookingOrMeal() {
        // given
        OrderTableChangeEmptyRequest orderTableChangeEmptyRequest = new OrderTableChangeEmptyRequest(true);
        OrderLineItem orderLineItem =
                OrderLineItem.of(1L, null,
                                 Menu.of(1L, Name.from("음식1"), Price.from(BigDecimal.ZERO), null,
                                         MenuProducts.from(Collections.emptyList())), 1);
        OrderTable orderTable =
                OrderTable.of(orderTable1.getId(), orderTable1.getTableGroup(),
                              orderTable1.getNumberOfGuests(), false,
                              Orders.from(Collections.singletonList(
                                      Order.of(1L, null, OrderStatus.COOKING, LocalDateTime.now(),
                                               OrderLineItems.from(Collections.singletonList(orderLineItem))))));

        given(orderTableRepository.findById(orderTable.getId())).willReturn(Optional.of(orderTable));

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
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 테이블은 방문한 손님 수를 등록할 수 없다.")
    @Test
    void cannotChangeNumberOfGuestsEmptyTable() {
        // given
        OrderTableChangeNumberOfGuestRequest orderTableChangeNumberOfGuestRequest = new OrderTableChangeNumberOfGuestRequest(
                1);

        given(orderTableRepository.findById(orderTable1.getId())).willReturn(Optional.of(orderTable1));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () ->
                tableService.changeNumberOfGuests(orderTable1.getId(), orderTableChangeNumberOfGuestRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
}
