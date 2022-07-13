package kitchenpos.order.application;

import kitchenpos.order.domain.MenuClient;
import kitchenpos.order.domain.Order;
import kitchenpos.order.dto.MenuResponse;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.exception.OrderMenusNotFoundException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {
    @Mock
    private MenuClient menuClient;

    @InjectMocks
    private OrderMapper orderMapper;

    @DisplayName("주문 요청을 주문 도메인으로 매핑한다.")
    @Test
    void mapFrom() {
        // given
        OrderRequest orderRequest = new OrderRequest(1L,
                                                     Arrays.asList(new OrderLineItemRequest(1L, 1),
                                                                   new OrderLineItemRequest(2L, 2)));

        given(menuClient.getMenus(orderRequest.getOrderLineItems()
                                          .stream()
                                          .map(OrderLineItemRequest::getMenuId)
                                          .collect(Collectors.toList())))
                .willReturn(orderRequest.getOrderLineItems()
                                    .stream()
                                    .map(orderLineItemRequest -> new MenuResponse(orderLineItemRequest.getMenuId(),
                                                                                  "음식" + orderLineItemRequest.getMenuId(),
                                                                                  orderLineItemRequest.getMenuId() * 1_000))
                                    .collect(Collectors.toList()));

        // when
        Order order = orderMapper.mapFrom(orderRequest);

        // then
        assertThat(order.getOrderTableId()).isEqualTo(orderRequest.getOrderTableId());
        assertThat(order.getOrderedTime()).isNotNull();
        assertThat(order.getOrderStatus()).isNull();
        assertThat(order.getOrderLineItems()).extracting("orderMenu")
                .extracting("menuId")
                .containsExactly(orderRequest.getOrderLineItems()
                                         .stream()
                                         .map(OrderLineItemRequest::getMenuId)
                                         .toArray());
    }

    @DisplayName("주문 메뉴가 존재하지 않으면 주문 요청을 주문 도메인으로 매핑할 수 없다.")
    @Test
    void cannotMapFromIfMenusNotFound() {
        // given
        OrderRequest orderRequest = new OrderRequest(1L,
                                                     Arrays.asList(new OrderLineItemRequest(1L, 1),
                                                                   new OrderLineItemRequest(2L, 2)));

        given(menuClient.getMenus(orderRequest.getOrderLineItems()
                                          .stream()
                                          .map(OrderLineItemRequest::getMenuId)
                                          .collect(Collectors.toList())))
                .willReturn(Collections.emptyList());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderMapper.mapFrom(orderRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(OrderMenusNotFoundException.class);
    }
}
