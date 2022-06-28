package kitchenpos.product.exception;

public class ProductNotFoundException extends RuntimeException {
    public static final String MESSAGE = "상품을 찾을 수 없습니다.";
    private static final long serialVersionUID = 8696573347996898067L;

    public ProductNotFoundException() {
        super(MESSAGE);
    }
}
