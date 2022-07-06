package kitchenpos.common.exception;

public class RequiredNameException extends RuntimeException {
    public static final String MESSAGE = "이름을 입력해주세요.";
    private static final long serialVersionUID = -4777339586310503907L;

    public RequiredNameException() {
        super(MESSAGE);
    }
}
