package kitchenpos.table.domain;

import kitchenpos.table.dto.OrderResponse;

public interface OrderClient {
    OrderResponse getOrder(Long orderId);
}
