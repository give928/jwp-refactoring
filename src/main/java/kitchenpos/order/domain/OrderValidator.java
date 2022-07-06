package kitchenpos.order.domain;

import kitchenpos.menu.domain.MenuRepository;
import kitchenpos.menu.exception.MenuNotFoundException;
import kitchenpos.order.exception.OrderNotCompletionException;
import kitchenpos.order.exception.RequiredOrderLineItemException;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
import kitchenpos.table.exception.OrderTableEmptyException;
import kitchenpos.table.exception.OrderTableNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class OrderValidator {
    private final OrderTableRepository orderTableRepository;
    private final MenuRepository menuRepository;

    public OrderValidator(final OrderTableRepository orderTableRepository, final MenuRepository menuRepository) {
        this.orderTableRepository = orderTableRepository;
        this.menuRepository = menuRepository;
    }

    public boolean create(Order order) {
        validateOrderTable(order.getOrderTableId());
        validateOrderLineItems(order.getOrderLineItems());
        return true;
    }

    private void validateOrderTable(Long orderTableId) {
        OrderTable orderTable = orderTableRepository.findById(orderTableId)
                .orElseThrow(OrderTableNotFoundException::new);
        if (orderTable.isEmpty()) {
            throw new OrderTableEmptyException();
        }
    }

    private void validateOrderLineItems(List<OrderLineItem> orderLineItems) {
        if (CollectionUtils.isEmpty(orderLineItems)) {
            throw new RequiredOrderLineItemException();
        }
        validateOrderLineItemMenus(orderLineItems);
    }

    private void validateOrderLineItemMenus(List<OrderLineItem> orderLineItems) {
        Long menuCount = menuRepository.countByIdIn(mapMenuIds(orderLineItems));
        if (orderLineItems.size() != menuCount.intValue()) {
            throw new MenuNotFoundException();
        }
    }

    private List<Long> mapMenuIds(List<OrderLineItem> orderLineItems) {
        return orderLineItems.stream()
                .map(OrderLineItem::getMenuId)
                .collect(Collectors.toList());
    }

    public boolean changeOrderStatus(Order order) {
        if (Objects.equals(OrderStatus.COMPLETION, order.getOrderStatus())) {
            throw new OrderNotCompletionException();
        }
        return true;
    }
}
