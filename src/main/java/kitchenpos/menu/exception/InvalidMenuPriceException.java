package kitchenpos.menu.exception;

public class InvalidMenuPriceException extends RuntimeException {
    public static final String MESSAGE = "메뉴의 가격은 메뉴 상품들의 금액의 합 이하만 가능합니다.";
    private static final long serialVersionUID = 3430014830194875266L;

    public InvalidMenuPriceException() {
        super(MESSAGE);
    }
}
