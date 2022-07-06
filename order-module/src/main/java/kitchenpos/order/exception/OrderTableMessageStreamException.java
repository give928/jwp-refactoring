package kitchenpos.order.exception;

public class OrderTableMessageStreamException extends RuntimeException {
    public static final String PAYLOAD_MESSAGE = "주문 생성(주문 테이블 확인) 이벤트 메시지에 전송할 주문 테이블 정보를 찾을 수 없습니다.(payload:%s)";
    public static final String SEND_AND_RECEIVE_MESSAGE = "주문 생성(주문 테이블 확인) 이벤트 메시지 송수신에 실패했습니다.(sendMessage:%s, receiveMessage:%s)";
    private static final long serialVersionUID = 5471065979825032844L;

    public OrderTableMessageStreamException(String payload) {
        super(String.format(PAYLOAD_MESSAGE, payload));
    }

    public OrderTableMessageStreamException(String sendMessage, String receiveMessage) {
        super(String.format(SEND_AND_RECEIVE_MESSAGE, sendMessage, receiveMessage));
    }
}
