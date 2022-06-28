package kitchenpos.order.domain;

import kitchenpos.order.exception.OrderNotCompletionException;
import kitchenpos.table.domain.OrderTableEmptyChangedEvent;
import kitchenpos.table.domain.OrderTableUngroupedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class OrderTableEventHandler {
    private final OrderRepository orderRepository;

    public OrderTableEventHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @EventListener
    public void handle(OrderTableEmptyChangedEvent event) {
        validateIfTableOrderStatusInCookingOrMeal(event.getId());
    }

    @EventListener
    public void handle(OrderTableUngroupedEvent event) {
        validateIfTablesOrderStatusInCookingOrMeal(event.getIds());
    }

    private void validateIfTablesOrderStatusInCookingOrMeal(List<Long> ids) {
        ids.forEach(this::validateIfTableOrderStatusInCookingOrMeal);
    }

    private void validateIfTableOrderStatusInCookingOrMeal(Long id) {
        if (hasOrderStatusInCookingOrMeal(id)) {
            throw new OrderNotCompletionException();
        }
    }

    private boolean hasOrderStatusInCookingOrMeal(Long id) {
        return orderRepository.existsByOrderTableIdAndOrderStatusIn(id, Arrays.asList(OrderStatus.COOKING, OrderStatus.MEAL));
    }
}
