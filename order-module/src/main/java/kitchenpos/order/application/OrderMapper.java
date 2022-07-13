package kitchenpos.order.application;

import kitchenpos.order.domain.MenuClient;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.dto.MenuResponse;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.exception.OrderMenusNotFoundException;
import kitchenpos.order.exception.RequiredOrderLineItemException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    private final MenuClient menuClient;

    public OrderMapper(MenuClient menuClient) {
        this.menuClient = menuClient;
    }

    public Order mapFrom(OrderRequest orderRequest) {
        return Order.of(orderRequest.getOrderTableId(), mapOrderLineItems(orderRequest));
    }

    private List<OrderLineItem> mapOrderLineItems(OrderRequest orderRequest) {
        List<OrderLineItemRequest> orderLineItemRequests = getOrderLineItemRequests(orderRequest);
        List<MenuResponse> menuResponses = getMenus(orderLineItemRequests);
        return orderLineItemRequests.stream()
                .map(orderLineItemRequest ->
                             OrderLineItem.of(menuResponses.stream()
                                                      .filter(menuResponse -> Objects.equals(menuResponse.getId(), orderLineItemRequest.getMenuId()))
                                                      .findAny()
                                                      .orElseThrow(OrderMenusNotFoundException::new)
                                                      .toOrderMenu(),
                                              orderLineItemRequest.getQuantity()))
                .collect(Collectors.toList());
    }

    private List<OrderLineItemRequest> getOrderLineItemRequests(OrderRequest orderRequest) {
        return Optional.ofNullable(orderRequest.getOrderLineItems())
                .filter(requests -> !requests.isEmpty())
                .orElseThrow(RequiredOrderLineItemException::new);
    }

    private List<MenuResponse> getMenus(List<OrderLineItemRequest> orderLineItemRequests) {
        List<MenuResponse> menuResponses = menuClient.getMenus(mapMenuIds(orderLineItemRequests));
        validateIfMenusNotFound(orderLineItemRequests, menuResponses);
        return menuResponses;
    }

    private List<Long> mapMenuIds(List<OrderLineItemRequest> orderLineItemRequests) {
        return orderLineItemRequests.stream()
                .map(OrderLineItemRequest::getMenuId)
                .collect(Collectors.toList());
    }

    private void validateIfMenusNotFound(List<OrderLineItemRequest> orderLineItemRequests, List<MenuResponse> menuResponses) {
        if (orderLineItemRequests.size() != menuResponses.size()) {
            throw new OrderMenusNotFoundException();
        }
    }
}
