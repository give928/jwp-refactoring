package kitchenpos.menu.exception;

public class RequiredMenuGroupException extends RuntimeException {
    public static final String MESSAGE = "메뉴 그룹을 입력해주세요.";
    private static final long serialVersionUID = 399600598810898003L;

    public RequiredMenuGroupException() {
        super(MESSAGE);
    }
}
