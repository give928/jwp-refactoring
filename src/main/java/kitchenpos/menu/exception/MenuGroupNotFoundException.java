package kitchenpos.menu.exception;

public class MenuGroupNotFoundException extends RuntimeException {
    public static final String MESSAGE = "메뉴 그룹을 찾을 수 없습니다.";
    private static final long serialVersionUID = -3502168050893020201L;

    public MenuGroupNotFoundException() {
        super(MESSAGE);
    }
}
