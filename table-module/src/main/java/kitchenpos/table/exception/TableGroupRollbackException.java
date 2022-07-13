package kitchenpos.table.exception;

public class TableGroupRollbackException extends RuntimeException {
    public static final String MESSAGE = "주문 미완료 메시지 처리 중 단체 지정에 속하는 주문 테이블이 단체 지정되어 있어서 되돌릴 수 없습니다.";
    private static final long serialVersionUID = -6302330427801043460L;

    public TableGroupRollbackException() {
        super(MESSAGE);
    }
}
