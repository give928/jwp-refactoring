package kitchenpos.common.exception;

public class RequiredPriceException extends RuntimeException {
    public static final String MESSAGE = "가격을 0원 이상으로 입력해주세요.";
    private static final long serialVersionUID = 2503869952053623293L;

    public RequiredPriceException() {
        super(MESSAGE);
    }
}
