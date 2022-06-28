package kitchenpos.table.exception;

public class RequiredOrderTablesOfTableGroupException extends RuntimeException {
    public static final String MESSAGE = "단체 지정할 주문 테이블을 2개 이상 입력해주세요.";
    private static final long serialVersionUID = -4032401461691714279L;

    public RequiredOrderTablesOfTableGroupException() {
        super(MESSAGE);
    }
}
