package kitchenpos.order.application;

import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuRepository;
import kitchenpos.order.domain.*;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.order.dto.OrderStatusChangeRequest;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderTableRepository orderTableRepository;

    public OrderService(final MenuRepository menuRepository, final OrderRepository orderRepository,
                        final OrderTableRepository orderTableRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.orderTableRepository = orderTableRepository;
    }

    @Transactional
    public OrderResponse create(final OrderRequest orderRequest) {
        final OrderTable orderTable = orderTableRepository.findById(orderRequest.getOrderTableId())
                .orElseThrow(IllegalArgumentException::new);
        List<OrderLineItem> orderLineItems = mapOrderLineItems(orderRequest);

        return OrderResponse.from(orderRepository.save(Order.of(orderTable, orderLineItems)));
    }

    private List<OrderLineItem> mapOrderLineItems(OrderRequest orderRequest) {
        List<Menu> menus = findMenus(orderRequest.getOrderLineItems());

        return orderRequest.getOrderLineItems().stream()
                .map(orderLineItemRequest ->
                             OrderLineItem.of(findMenu(menus, orderLineItemRequest.getMenuId()),
                                              orderLineItemRequest.getQuantity()))
                .collect(Collectors.toList());
    }

    private List<Menu> findMenus(List<OrderLineItemRequest> orderLineItemRequests) {
        validateOrderLineItemRequests(orderLineItemRequests);
        return menuRepository.findByIdIn(orderLineItemRequests.stream()
                                                 .map(OrderLineItemRequest::getMenuId)
                                                 .collect(Collectors.toList()));
    }

    private Menu findMenu(List<Menu> menus, Long menuId) {
        return menus.stream()
                .filter(menu -> menu.getId().equals(menuId))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    private void validateOrderLineItemRequests(List<OrderLineItemRequest> orderLineItemRequests) {
        if (CollectionUtils.isEmpty(orderLineItemRequests)) {
            throw new IllegalArgumentException();
        }
    }

    public List<OrderResponse> list() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse changeOrderStatus(final Long orderId,
                                           final OrderStatusChangeRequest orderStatusChangeRequest) {
        final Order savedOrder = orderRepository.findById(orderId)
                .orElseThrow(IllegalArgumentException::new);
        return OrderResponse.from(
                savedOrder.changeOrderStatus(OrderStatus.valueOf(orderStatusChangeRequest.getOrderStatus())));
    }
}
