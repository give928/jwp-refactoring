package kitchenpos.order.domain;

import kitchenpos.menu.domain.MenuRepository;
import kitchenpos.menu.exception.MenuNotFoundException;
import kitchenpos.order.exception.OrderNotCompletionException;
import kitchenpos.order.exception.RequiredOrderLineItemException;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
import kitchenpos.table.exception.OrderTableEmptyException;
import kitchenpos.table.exception.OrderTableNotFoundException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderValidatorTest {
    @Mock
    private OrderTableRepository orderTableRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private OrderValidator orderValidator;

    @DisplayName("주문 생성 유효성을 확인한다.")
    @Test
    void create() {
        // given
        OrderTable orderTable = aOrderTable().empty(false).build();
        Order order = aOrder1().build();

        given(orderTableRepository.findById(order.getOrderTableId())).willReturn(Optional.of(orderTable));
        given(menuRepository.countByIdIn(order.getOrderLineItems().stream()
                                                 .map(OrderLineItem::getMenuId)
                                                 .collect(Collectors.toList())))
                .willReturn(Long.valueOf(order.getOrderLineItems().size()));

        // when
        boolean valid = orderValidator.create(order);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("주문 항목이 1개 미만이면 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfEmptyOrderLineItem() {
        // given
        List<OrderLineItem> orderLineItems = Collections.emptyList();
        OrderTable orderTable = aOrderTable().empty(false).build();
        Order order = aOrder1().orderLineItems(orderLineItems).build();

        given(orderTableRepository.findById(orderTable.getId())).willReturn(Optional.of(orderTable));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(RequiredOrderLineItemException.class)
                .hasMessageContaining(RequiredOrderLineItemException.MESSAGE);
    }

    @DisplayName("등록되지 않은 메뉴는 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfNotExistsMenu() {
        // given
        Long menuId = -1L;
        List<OrderLineItem> orderLineItems = Collections.singletonList(OrderLineItem.of(menuId, 1));
        OrderTable orderTable = aOrderTable().empty(false).build();
        Order order = aOrder1().orderLineItems(orderLineItems).build();

        given(orderTableRepository.findById(order.getOrderTableId())).willReturn(Optional.of(orderTable));
        given(menuRepository.countByIdIn(order.getOrderLineItems().stream()
                                                 .map(OrderLineItem::getMenuId)
                                                 .collect(Collectors.toList())))
                .willReturn(0L);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(MenuNotFoundException.class)
                .hasMessageContaining(MenuNotFoundException.MESSAGE);
    }

    @DisplayName("등록되지 않은 주문 테이블은 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfNotExistsOrderTable() {
        // given
        Long orderTableId = -1L;
        Order order = aOrder1().orderTableId(orderTableId).build();

        given(orderTableRepository.findById(order.getOrderTableId())).willReturn(Optional.empty());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableNotFoundException.class)
                .hasMessageContaining(OrderTableNotFoundException.MESSAGE);
    }

    @DisplayName("빈 테이블은 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfEmptyOrderTable() {
        // given
        Order order = aOrder1().build();
        OrderTable orderTable = aOrderTable().id(order.getOrderTableId()).empty(true).build();

        given(orderTableRepository.findById(order.getOrderTableId())).willReturn(Optional.of(orderTable));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderTableEmptyException.class)
                .hasMessageContaining(OrderTableEmptyException.EMPTY_MESSAGE);
    }

    @DisplayName("주문 상태를 변경 유효성을 확인한다.")
    @Test
    void changeOrderStatus() {
        // given
        Order order = aOrder1().build();

        // when
        boolean valid = orderValidator.changeOrderStatus(order);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("주문 상태가 완료인 경우 주문 상태 변경 유효성 확인이 실패한다.")
    @Test
    void cannotChangeOrderStatusIfCompletion() {
        // given
        Order order = aOrder1().build();
        order.changeOrderStatus(aOrderValidator(), OrderStatus.COMPLETION);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.changeOrderStatus(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderNotCompletionException.class)
                .hasMessageContaining(OrderNotCompletionException.MESSAGE);
    }
}
