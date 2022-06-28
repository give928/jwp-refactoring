package kitchenpos.table.exception;

public class OrderTableNotFoundException extends RuntimeException {
    public static final String MESSAGE = "주문 테이블을 찾을 수 없습니다.";
    private static final long serialVersionUID = -2100920650192242906L;

    public OrderTableNotFoundException() {
        super(MESSAGE);
    }
}
