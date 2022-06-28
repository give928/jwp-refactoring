package kitchenpos.menu.exception;

public class MenuNotFoundException extends RuntimeException {
    public static final String MESSAGE = "메뉴를 찾을 수 없습니다.";
    private static final long serialVersionUID = 4392530987218240987L;

    public MenuNotFoundException() {
        super(MESSAGE);
    }
}
