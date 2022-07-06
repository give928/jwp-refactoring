package kitchenpos.order.application;

import kitchenpos.order.domain.*;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.order.dto.OrderStatusChangeRequest;
import kitchenpos.order.exception.OrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(final OrderRepository orderRepository, final OrderValidator orderValidator,
                        final OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderValidator = orderValidator;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public OrderResponse create(final OrderRequest orderRequest) {
        Order order = Order.of(orderRequest.getOrderTableId(), mapOrderLineItems(orderRequest), orderValidator, orderEventPublisher);
        return OrderResponse.from(orderRepository.save(order));
    }

    private List<OrderLineItem> mapOrderLineItems(OrderRequest orderRequest) {
        return Optional.ofNullable(orderRequest.getOrderLineItems())
                .orElse(Collections.emptyList())
                .stream()
                .map(orderLineItemRequest -> OrderLineItem.of(orderLineItemRequest.getMenuId(),
                                                              orderLineItemRequest.getQuantity()))
                .collect(Collectors.toList());
    }

    public List<OrderResponse> list() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse changeOrderStatus(final Long orderId, final OrderStatusChangeRequest orderStatusChangeRequest) {
        final Order savedOrder = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
        return OrderResponse.from(savedOrder.changeOrderStatus(orderValidator, orderStatusChangeRequest.toOrderStatus()));
    }
}
