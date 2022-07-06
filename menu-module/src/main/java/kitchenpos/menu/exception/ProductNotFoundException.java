package kitchenpos.menu.exception;

public class ProductNotFoundException extends RuntimeException {
    public static final String MESSAGE = "상품을 찾을 수 없습니다.";
    private static final long serialVersionUID = 6355901650723864465L;

    public ProductNotFoundException() {
        super(MESSAGE);
    }
}
