package kitchenpos.order.exception;

public class RequiredOrderLineItemException extends RuntimeException {
    public static final String MESSAGE = "주문 항목을 1개 이상 입력해주세요.";
    private static final long serialVersionUID = -5644743105890722017L;

    public RequiredOrderLineItemException() {
        super(MESSAGE);
    }
}
