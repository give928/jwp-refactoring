package kitchenpos.table.exception;

public class TableGroupNotFoundException extends RuntimeException {
    public static final String MESSAGE = "단체 지정을 찾을 수 없습니다.";
    private static final long serialVersionUID = 88823509446476873L;

    public TableGroupNotFoundException() {
        super(MESSAGE);
    }
}
