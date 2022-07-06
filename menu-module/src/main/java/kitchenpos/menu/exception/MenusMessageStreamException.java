package kitchenpos.menu.exception;

public class MenusMessageStreamException extends RuntimeException {
    public static final String PAYLOAD_MESSAGE = "수신한 이벤트 메시지에서 메뉴 정보를 찾을 수 없습니다.(payload:%s)";
    private static final long serialVersionUID = 4213322934341494670L;

    public MenusMessageStreamException(String payload) {
        super(String.format(PAYLOAD_MESSAGE, payload));
    }
}
