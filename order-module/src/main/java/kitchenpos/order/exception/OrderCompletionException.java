package kitchenpos.order.exception;

public class OrderCompletionException extends RuntimeException {
    public static final String MESSAGE = "완료된 주문은 변경할 수 없습니다.";
    private static final long serialVersionUID = -7758469926779291488L;

    public OrderCompletionException() {
        super(MESSAGE);
    }
}
