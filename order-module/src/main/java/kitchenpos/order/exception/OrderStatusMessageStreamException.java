package kitchenpos.order.exception;

public class OrderStatusMessageStreamException extends RuntimeException {
    public static final String PAYLOAD_MESSAGE = "수신한 이벤트 메시지에서 주문 테이블 정보를 찾을 수 없습니다.(payload:%s)";
    private static final long serialVersionUID = 5471065979825032844L;

    public OrderStatusMessageStreamException(String payload) {
        super(String.format(PAYLOAD_MESSAGE, payload));
    }
}
