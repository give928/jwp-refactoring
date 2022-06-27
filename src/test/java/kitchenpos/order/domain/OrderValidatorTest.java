package kitchenpos.order.domain;

import kitchenpos.menu.domain.MenuRepository;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
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
        Order order = aOrder1();

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
        Order order = Order.of(1L, orderTable.getId(), orderLineItems, aOrderValidator());

        given(orderTableRepository.findById(orderTable.getId())).willReturn(Optional.of(orderTable));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("등록되지 않은 메뉴는 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfNotExistsMenu() {
        // given
        Long menuId = -1L;
        List<OrderLineItem> orderLineItems = Collections.singletonList(OrderLineItem.of(menuId, 1));
        OrderTable orderTable = aOrderTable().empty(false).build();
        Order order = Order.of(1L, orderTable.getId(), orderLineItems, aOrderValidator());

        given(orderTableRepository.findById(order.getOrderTableId())).willReturn(Optional.of(orderTable));
        given(menuRepository.countByIdIn(order.getOrderLineItems().stream()
                                                 .map(OrderLineItem::getMenuId)
                                                 .collect(Collectors.toList())))
                .willReturn(0L);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("등록되지 않은 주문 테이블은 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfNotExistsOrderTable() {
        // given
        Long orderTableId = -1L;
        Order order = Order.of(1L, orderTableId, aOrderLineItems1().get(), aOrderValidator());

        given(orderTableRepository.findById(order.getOrderTableId())).willReturn(Optional.empty());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 테이블은 주문 생성 유효성 확인이 실패한다.")
    @Test
    void cannotCreateIfEmptyOrderTable() {
        // given
        OrderTable orderTable = aOrderTable().empty(true).build();
        Order order = Order.of(1L, orderTable.getId(), aOrderLineItems1().get(), aOrderValidator());

        given(orderTableRepository.findById(order.getOrderTableId())).willReturn(Optional.of(orderTable));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 상태를 변경 유효성을 확인한다.")
    @Test
    void changeOrderStatus() {
        // given
        Order order = aOrder1();

        // when
        boolean valid = orderValidator.changeOrderStatus(order);

        // then
        assertThat(valid).isTrue();
    }

    @DisplayName("주문 상태가 완료인 경우 주문 상태 변경 유효성 확인이 실패한다.")
    @Test
    void cannotChangeOrderStatusIfCompletion() {
        // given
        Order order = aOrder1();
        order.changeOrderStatus(aOrderValidator(), OrderStatus.COMPLETION);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderValidator.changeOrderStatus(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
}
