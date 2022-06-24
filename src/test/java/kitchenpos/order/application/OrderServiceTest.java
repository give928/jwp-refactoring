package kitchenpos.order.application;

import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuProducts;
import kitchenpos.menu.domain.MenuRepository;
import kitchenpos.order.domain.*;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.order.dto.OrderStatusChangeRequest;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
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

import java.math.BigDecimal;
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
class OrderServiceTest {
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderTableRepository orderTableRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order1;
    private Order order2;

    public static Stream<List<OrderLineItemRequest>> invalidOrderLineItemsParameter() {
        return Stream.of(null, Collections.emptyList());
    }

    @BeforeEach
    void setUp() {
        Menu menu1 = Menu.of(1L, "메뉴1", BigDecimal.ZERO, null, MenuProducts.from(Collections.emptyList()));
        Menu menu2 = Menu.of(2L, "메뉴2", BigDecimal.ZERO, null, MenuProducts.from(Collections.emptyList()));

        OrderTable orderTable1 = OrderTable.of(1L, null, 1, false);
        OrderTable orderTable2 = OrderTable.of(2L, null, 1, false);

        Long orderId1 = 1L;
        OrderLineItem orderLineItem1 = OrderLineItem.of(1L, order1, menu1, 1);
        OrderLineItem orderLineItem2 = OrderLineItem.of(2L, order1, menu2, 2);
        order1 = Order.of(orderId1, orderTable1, OrderLineItems.from(Arrays.asList(orderLineItem1, orderLineItem2)));

        Long orderId2 = 2L;
        OrderLineItem orderLineItem3 = OrderLineItem.of(3L, order2, menu1, 3);
        OrderLineItem orderLineItem4 = OrderLineItem.of(4L, order2, menu2, 4);
        order2 = Order.of(orderId2, orderTable2, OrderLineItems.from(Arrays.asList(orderLineItem3, orderLineItem4)));
    }

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() {
        // given
        OrderRequest orderRequest = createOrderRequestBy(order1);

        given(menuRepository.findByIdIn(orderRequest.getOrderLineItems().stream()
                                                .mapToLong(OrderLineItemRequest::getMenuId)
                                                .boxed()
                                                .collect(Collectors.toList())))
                .willReturn(order1.getOrderLineItems().stream()
                                    .map(OrderLineItem::getMenu)
                                    .collect(Collectors.toList()));
        given(orderTableRepository.findById(orderRequest.getOrderTableId())).willReturn(Optional.of(order1.getOrderTable()));
        given(orderRepository.save(any())).willReturn(order1);

        // when
        OrderResponse orderResponse = orderService.create(orderRequest);

        // then
        assertThat(orderResponse.getId()).isEqualTo(order1.getId());
        assertThat(orderResponse.getOrderTableId()).isEqualTo(order1.getOrderTable().getId());
        assertThat(orderResponse.getOrderStatus()).isEqualTo(order1.getOrderStatus().name());
        assertThat(orderResponse.getOrderLineItems()).hasSameSizeAs(order1.getOrderLineItems());
    }

    @DisplayName("주문 항목을 1개 이상 입력해야 한다.")
    @ParameterizedTest
    @MethodSource("invalidOrderLineItemsParameter")
    void invalidOrderLineItems(List<OrderLineItemRequest> orderLineItems) {
        // given
        OrderRequest orderRequest = new OrderRequest(order1.getOrderTable().getId(), orderLineItems);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(orderRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 항목들은 등록된 메뉴만 가능하다.")
    @Test
    void invalidMenuIdOfOrderLineItem() {
        // given
        Long menuId = -1L;
        OrderRequest orderRequest = new OrderRequest(order1.getOrderTable().getId(),
                                                     Collections.singletonList(new OrderLineItemRequest(menuId, 1)));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(orderRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("등록된 주문 테이블만 주문을 할 수 있다.")
    @Test
    void notExistsOrderTable() {
        // given
        OrderRequest orderRequest = new OrderRequest(-1L,
                                                     getOrderLineItemRequests(order1));

        given(orderTableRepository.findById(orderRequest.getOrderTableId())).willReturn(Optional.empty());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(orderRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 테이블은 주문을 할 수 없다.")
    @Test
    void cannotEmptyOrderTable() {
        // given
        OrderTable orderTable1 = OrderTable.of(1L, null, 1, true);
        OrderRequest orderRequest = new OrderRequest(orderTable1.getId(),
                                                     getOrderLineItemRequests(order1));

        given(menuRepository.findByIdIn(orderRequest.getOrderLineItems().stream()
                                                .mapToLong(OrderLineItemRequest::getMenuId)
                                                .boxed()
                                                .collect(Collectors.toList())))
                .willReturn(order1.getOrderLineItems().stream()
                                    .map(OrderLineItem::getMenu)
                                    .collect(Collectors.toList()));
        given(orderTableRepository.findById(orderRequest.getOrderTableId())).willReturn(Optional.of(orderTable1));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(orderRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문과 주문 항목의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        given(orderRepository.findAll()).willReturn(Arrays.asList(order1, order2));

        // when
        List<OrderResponse> orderResponses = orderService.list();

        // then
        assertThat(orderResponses).extracting("id").containsExactly(order1.getId(), order2.getId());
        assertThat(orderResponses).extracting("orderTableId").containsExactly(order1.getOrderTable().getId(),
                                                                              order2.getOrderTable().getId());
        assertThat(orderResponses).extracting("orderStatus").containsExactly(order1.getOrderStatus().name(),
                                                                             order2.getOrderStatus().name());
        assertThat(orderResponses).extracting("orderedTime").containsExactly(order1.getOrderedTime(),
                                                                             order2.getOrderedTime());
    }

    @DisplayName("주문의 주문 상태를 변경하고 주문과 주문 항목을 반환한다.")
    @Test
    void changeOrderStatus() {
        // given
        String nextOrderStatus = OrderStatus.MEAL.name();
        OrderStatusChangeRequest orderStatusChangeRequest = new OrderStatusChangeRequest(nextOrderStatus);

        given(orderRepository.findById(order1.getId())).willReturn(Optional.of(order1));

        // when
        OrderResponse orderResponse = orderService.changeOrderStatus(order1.getId(), orderStatusChangeRequest);

        // then
        assertThat(orderResponse.getOrderStatus()).isEqualTo(nextOrderStatus);
    }

    private OrderRequest createOrderRequestBy(Order order) {
        return new OrderRequest(order.getOrderTable().getId(),
                                getOrderLineItemRequests(order));
    }

    private List<OrderLineItemRequest> getOrderLineItemRequests(Order order) {
        return order.getOrderLineItems().stream()
                .map(orderLineItem -> new OrderLineItemRequest(orderLineItem.getMenu().getId(),
                                                               orderLineItem.getQuantity()))
                .collect(Collectors.toList());
    }
}
