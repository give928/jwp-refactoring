package kitchenpos.order.application;

import kitchenpos.menu.dao.MenuDao;
import kitchenpos.order.dao.OrderDao;
import kitchenpos.order.dao.OrderLineItemDao;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.order.dto.OrderStatusChangeRequest;
import kitchenpos.table.dao.OrderTableDao;
import kitchenpos.table.domain.OrderTable;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private MenuDao menuDao;
    @Mock
    private OrderDao orderDao;
    @Mock
    private OrderLineItemDao orderLineItemDao;
    @Mock
    private OrderTableDao orderTableDao;

    @InjectMocks
    private OrderService orderService;

    private OrderTable orderTable1;
    private OrderLineItem orderLineItem1;
    private OrderLineItem orderLineItem2;
    private Order order1;
    private Order order2;

    public static Stream<List<OrderLineItemRequest>> invalidOrderLineItemsParameter() {
        return Stream.of(null, Collections.emptyList());
    }

    @BeforeEach
    void setUp() {
        Long menuId1 = 1L;
        Long menuId2 = 2L;

        orderTable1 = new OrderTable(1L, null, 1, false);
        OrderTable orderTable2 = new OrderTable(2L, null, 1, false);

        Long orderId1 = 1L;
        orderLineItem1 = new OrderLineItem(1L, orderId1, menuId1, 1);
        orderLineItem2 = new OrderLineItem(2L, orderId1, menuId2, 2);
        order1 = new Order(orderId1, orderTable1.getId(), OrderStatus.COOKING.name(), LocalDateTime.now(), Arrays.asList(
                orderLineItem1, orderLineItem2));

        Long orderId2 = 2L;
        OrderLineItem orderLineItem3 = new OrderLineItem(3L, orderId2, menuId1, 3);
        OrderLineItem orderLineItem4 = new OrderLineItem(4L, orderId2, menuId2, 4);
        order2 = new Order(orderId2, orderTable2.getId(), OrderStatus.COOKING.name(), LocalDateTime.now(), Arrays.asList(
                orderLineItem3, orderLineItem4));
    }

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() {
        // given
        OrderLineItemRequest orderLineItemRequest1 = new OrderLineItemRequest(orderLineItem1.getMenuId(), orderLineItem1.getQuantity());
        OrderLineItemRequest orderLineItemRequest2 = new OrderLineItemRequest(orderLineItem2.getMenuId(), orderLineItem2.getQuantity());
        OrderRequest orderRequest = new OrderRequest(order1.getOrderTableId(), Arrays.asList(orderLineItemRequest1, orderLineItemRequest2));

        given(menuDao.countByIdIn(orderRequest.getOrderLineItems().stream()
                                          .mapToLong(OrderLineItemRequest::getMenuId)
                                          .boxed()
                                          .collect(Collectors.toList())))
                .willReturn(Long.valueOf(order1.getOrderLineItems().size()));
        given(orderTableDao.findById(orderRequest.getOrderTableId())).willReturn(Optional.of(orderTable1));
        given(orderDao.save(any())).willReturn(order1);
        given(orderLineItemDao.save(argThat(argument -> argument != null && Objects.equals(argument.getMenuId(), orderLineItem1.getMenuId()))))
                .willReturn(orderLineItem1);
        given(orderLineItemDao.save(argThat(argument -> argument != null && Objects.equals(argument.getMenuId(), orderLineItem2.getMenuId()))))
                .willReturn(orderLineItem2);

        // when
        OrderResponse orderResponse = orderService.create(orderRequest);

        // then
        assertThat(orderResponse.getId()).isEqualTo(order1.getId());
        assertThat(orderResponse.getOrderTableId()).isEqualTo(order1.getOrderTableId());
        assertThat(orderResponse.getOrderStatus()).isEqualTo(order1.getOrderStatus());
        assertThat(orderResponse.getOrderLineItems()).hasSameSizeAs(order1.getOrderLineItems());
    }

    @DisplayName("주문 항목을 1개 이상 입력해야 한다.")
    @ParameterizedTest
    @MethodSource("invalidOrderLineItemsParameter")
    void invalidOrderLineItems(List<OrderLineItemRequest> orderLineItems) {
        // given
        OrderRequest orderRequest = new OrderRequest(order1.getOrderTableId(), orderLineItems);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(orderRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 항목들은 등록된 메뉴만 가능하다.")
    @Test
    void invalidMenuIdOfOrderLineItem() {
        // given
        Long menuId = 0L;
        OrderRequest orderRequest = new OrderRequest(order1.getOrderTableId(), Collections.singletonList(new OrderLineItemRequest(menuId, 1)));

        given(menuDao.countByIdIn(Collections.singletonList(menuId))).willReturn(0L);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(orderRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("등록된 주문 테이블만 주문을 할 수 있다.")
    @Test
    void notExistsOrderTable() {
        // given
        OrderRequest orderRequest = new OrderRequest(0L, Arrays.asList(
                new OrderLineItemRequest(orderLineItem1.getMenuId(), orderLineItem1.getQuantity()),
                new OrderLineItemRequest(orderLineItem2.getMenuId(), orderLineItem2.getQuantity())));

        given(menuDao.countByIdIn(orderRequest.getOrderLineItems().stream()
                                          .mapToLong(OrderLineItemRequest::getMenuId)
                                          .boxed()
                                          .collect(Collectors.toList()))).willReturn(Long.valueOf(order1.getOrderLineItems().size()));
        given(orderTableDao.findById(orderRequest.getOrderTableId())).willReturn(Optional.empty());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(orderRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 테이블은 주문을 할 수 없다.")
    @Test
    void cannotEmptyOrderTable() {
        // given
        OrderTable orderTable1 = new OrderTable(1L, null, 1, true);
        OrderRequest orderRequest = new OrderRequest(order1.getOrderTableId(),
                                                     Arrays.asList(new OrderLineItemRequest(orderLineItem1.getMenuId(), orderLineItem1.getQuantity()),
                                                                   new OrderLineItemRequest(orderLineItem2.getMenuId(), orderLineItem2.getQuantity())));

        given(menuDao.countByIdIn(orderRequest.getOrderLineItems().stream()
                                          .mapToLong(OrderLineItemRequest::getMenuId)
                                          .boxed()
                                          .collect(Collectors.toList()))).willReturn(Long.valueOf(order1.getOrderLineItems().size()));
        given(orderTableDao.findById(orderRequest.getOrderTableId())).willReturn(Optional.of(orderTable1));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(orderRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문과 주문 항목의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        Order order1 = new Order(this.order1.getId(), this.order1.getOrderTableId(), this.order1.getOrderStatus(), this.order1.getOrderedTime(), Collections.emptyList());
        Order order2 = new Order(this.order2.getId(), this.order2.getOrderTableId(), this.order2.getOrderStatus(), this.order2.getOrderedTime(), Collections.emptyList());

        given(orderDao.findAll()).willReturn(Arrays.asList(order1, order2));
        given(orderLineItemDao.findAllByOrderId(order1.getId())).willReturn(this.order1.getOrderLineItems());
        given(orderLineItemDao.findAllByOrderId(order2.getId())).willReturn(this.order2.getOrderLineItems());

        // when
        List<OrderResponse> orderResponses = orderService.list();

        // then
        assertThat(orderResponses).extracting("id").containsExactly(order1.getId(), order2.getId());
        assertThat(orderResponses).extracting("orderTableId").containsExactly(order1.getOrderTableId(), order2.getOrderTableId());
        assertThat(orderResponses).extracting("orderStatus").containsExactly(order1.getOrderStatus(), order2.getOrderStatus());
        assertThat(orderResponses).extracting("orderedTime").containsExactly(order1.getOrderedTime(), order2.getOrderedTime());
    }

    @DisplayName("주문의 주문 상태를 변경하고 주문과 주문 항목을 반환한다.")
    @Test
    void changeOrderStatus() {
        // given
        String nextOrderStatus = OrderStatus.MEAL.name();
        OrderStatusChangeRequest orderStatusChangeRequest = new OrderStatusChangeRequest(nextOrderStatus);

        given(orderDao.findById(order1.getId())).willReturn(Optional.of(order1));
        given(orderLineItemDao.findAllByOrderId(order1.getId())).willReturn(order1.getOrderLineItems());

        // when
        OrderResponse orderResponse = orderService.changeOrderStatus(order1.getId(), orderStatusChangeRequest);

        // then
        assertThat(orderResponse.getOrderStatus()).isEqualTo(nextOrderStatus);
    }
}
