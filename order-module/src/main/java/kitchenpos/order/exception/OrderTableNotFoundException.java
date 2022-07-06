package kitchenpos.order.exception;

public class OrderTableNotFoundException extends RuntimeException {
    public static final String MESSAGE = "주문 테이블을 찾을 수 없습니다.";
    private static final long serialVersionUID = -6139282864681872077L;

    public OrderTableNotFoundException() {
        super(MESSAGE);
    }
}
