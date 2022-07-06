package kitchenpos.table.exception;

public class OrderTableMessageStreamException extends RuntimeException {
    public static final String PAYLOAD_MESSAGE = "주문 테이블 확인 이벤트 메시지에서 주문 테이블 정보를 찾을 수 없습니다.(payload:%s)";
    private static final long serialVersionUID = 1787483487365073022L;

    public OrderTableMessageStreamException(String payload) {
        super(String.format(PAYLOAD_MESSAGE, payload));
    }
}
