package kitchenpos.order.exception;

public class OrderTableEmptyException extends RuntimeException {
    public static final String MESSAGE = "빈 테이블은 주문을 할 수 없습니다.";
    private static final long serialVersionUID = 7359570969960942167L;

    public OrderTableEmptyException() {
        super(MESSAGE);
    }
}
