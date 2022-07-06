package kitchenpos.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public static final String MESSAGE = "주문을 찾을 수 없습니다.";
    private static final long serialVersionUID = 1697339468987241009L;

    public OrderNotFoundException() {
        super(MESSAGE);
    }
}
