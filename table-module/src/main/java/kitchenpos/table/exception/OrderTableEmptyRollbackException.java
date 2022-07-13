package kitchenpos.table.exception;

public class OrderTableEmptyRollbackException extends RuntimeException {
    public static final String MESSAGE = "주문 미완료 메시지 처리 중 주문 테이블의 빈 테이블 여부가 이미 변경되어서 되돌릴 수 없습니다.";
    private static final long serialVersionUID = 8967391471968483963L;

    public OrderTableEmptyRollbackException() {
        super(MESSAGE);
    }
}
