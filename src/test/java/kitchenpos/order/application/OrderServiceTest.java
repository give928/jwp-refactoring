package kitchenpos.order.application;

import kitchenpos.order.domain.*;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.order.dto.OrderStatusChangeRequest;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kitchenpos.Fixtures.aOrder1;
import static kitchenpos.Fixtures.aOrder2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderValidator orderValidator;

    @InjectMocks
    private OrderService orderService;

    private Order order1;
    private Order order2;

    public static Stream<List<OrderLineItemRequest>> invalidOrderLineItemsParameter() {
        return Stream.of(null, Collections.emptyList());
    }

    @BeforeEach
    void setUp() {
        order1 = aOrder1().build();
        order2 = aOrder2().build();
    }

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() {
        // given
        OrderRequest orderRequest = createOrderRequestBy(order1);

        given(orderRepository.save(any())).willReturn(order1);
        given(orderValidator.create(any())).willReturn(true);

        // when
        OrderResponse orderResponse = orderService.create(orderRequest);

        // then
        assertThat(orderResponse.getId()).isEqualTo(order1.getId());
        assertThat(orderResponse.getOrderTableId()).isEqualTo(order1.getOrderTableId());
        assertThat(orderResponse.getOrderStatus()).isEqualTo(order1.getOrderStatus().name());
        assertThat(orderResponse.getOrderLineItems()).hasSameSizeAs(order1.getOrderLineItems());
    }

    @DisplayName("주문 항목을 1개 이상 입력해야 한다.")
    @ParameterizedTest
    @MethodSource("invalidOrderLineItemsParameter")
    void invalidOrderLineItems(List<OrderLineItemRequest> orderLineItems) {
        // given
        OrderRequest orderRequest = new OrderRequest(order1.getOrderTableId(), orderLineItems);

        given(orderValidator.create(any())).willThrow(IllegalArgumentException.class);

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
        OrderRequest orderRequest = new OrderRequest(order1.getOrderTableId(),
                                                     Collections.singletonList(new OrderLineItemRequest(menuId, 1)));

        given(orderValidator.create(any())).willThrow(IllegalArgumentException.class);

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

        given(orderValidator.create(any())).willThrow(IllegalArgumentException.class);

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

        given(orderValidator.create(any())).willThrow(IllegalArgumentException.class);

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
        assertThat(orderResponses).extracting("orderTableId").containsExactly(order1.getOrderTableId(),
                                                                              order2.getOrderTableId());
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
        return new OrderRequest(order.getOrderTableId(),
                                getOrderLineItemRequests(order));
    }

    private List<OrderLineItemRequest> getOrderLineItemRequests(Order order) {
        return order.getOrderLineItems().stream()
                .map(orderLineItem -> new OrderLineItemRequest(orderLineItem.getMenuId(),
                                                               orderLineItem.getQuantity()))
                .collect(Collectors.toList());
    }
}
