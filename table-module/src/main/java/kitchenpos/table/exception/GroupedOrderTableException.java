package kitchenpos.table.exception;

public class GroupedOrderTableException extends RuntimeException {
    public static final String MESSAGE = "단체 지정된 주문 테이블은 처리할 수 없습니다.";
    private static final long serialVersionUID = -1788629166828177843L;

    public GroupedOrderTableException() {
        super(MESSAGE);
    }
}
