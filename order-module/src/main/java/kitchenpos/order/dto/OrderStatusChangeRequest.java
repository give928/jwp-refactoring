package kitchenpos.order.dto;

import kitchenpos.order.domain.OrderStatus;

public class OrderStatusChangeRequest {
    private String orderStatus;

    public OrderStatusChangeRequest() {
    }

    public OrderStatusChangeRequest(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public OrderStatus toOrderStatus() {
        return OrderStatus.valueOf(orderStatus);
    }
}
