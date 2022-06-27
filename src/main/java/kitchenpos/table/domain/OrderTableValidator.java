package kitchenpos.table.domain;

import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@Transactional(readOnly = true)
public class OrderTableValidator implements TableValidator {
    private final OrderRepository orderRepository;

    public OrderTableValidator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public boolean clearTableGroup(OrderTable orderTable) {
        validateIfOrdersInCookingOrMeal(orderTable.getId());
        return true;
    }

    @Override
    public boolean changeEmpty(OrderTable orderTable) {
        validateIfNotNullTableGroup(orderTable);
        validateIfOrdersInCookingOrMeal(orderTable.getId());
        return true;
    }

    private void validateIfOrdersInCookingOrMeal(Long id) {
        if (hasOrderStatusInCookingOrMeal(id)) {
            throw new IllegalArgumentException();
        }
    }

    private boolean hasOrderStatusInCookingOrMeal(Long id) {
        return orderRepository.findByOrderTableId(id)
                .stream()
                .anyMatch(Order::isCookingOrMeal);
    }

    private void validateIfNotNullTableGroup(OrderTable orderTable) {
        if (Objects.nonNull(orderTable.getTableGroup())) {
            throw new IllegalArgumentException();
        }
    }

    public boolean changeNumberOfGuests(OrderTable orderTable) {
        validateIfEmpty(orderTable, Boolean.TRUE);
        return true;
    }
}
