package kitchenpos.table.exception;

public class OrderTableEmptyException extends RuntimeException {
    public static final String EMPTY_MESSAGE = "주문 테이블이 비어있는 경우 처리할 수 없습니다.";
    public static final String NOT_EMPTY_MESSAGE = "주문 테이블이 비어있지 않은 경우 처리할 수 없습니다.";
    private static final long serialVersionUID = -287577008418584917L;

    public OrderTableEmptyException() {
        super(EMPTY_MESSAGE);
    }

    public OrderTableEmptyException(String message) {
        super(message);
    }

    public static OrderTableEmptyException throwBy(boolean empty) {
        if (empty) {
            return new OrderTableEmptyException(EMPTY_MESSAGE);
        }
        return new OrderTableEmptyException(NOT_EMPTY_MESSAGE);
    }
}
