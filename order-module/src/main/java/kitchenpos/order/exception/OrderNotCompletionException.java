package kitchenpos.order.exception;

public class OrderNotCompletionException extends RuntimeException {
    public static final String MESSAGE = "주문이 조리 중이거나 식사 중인 경우 처리할 수 없습니다.";
    private static final long serialVersionUID = 3888835122086357489L;

    public OrderNotCompletionException() {
        super(MESSAGE);
    }
}
