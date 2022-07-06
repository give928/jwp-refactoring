package kitchenpos.table.exception;

public class OrderStatusMessageStreamException extends RuntimeException {
    public static final String PAYLOAD_MESSAGE = "주문 테이블의 빈 테이블 여부 변경 이벤트 메시지에 전송할 주문 테이블 정보를 찾을 수 없습니다.(payload:%s)";
    public static final String SEND_AND_RECEIVE_MESSAGE = "주문 테이블의 빈 테이블 여부 변경 이벤트 메시지 송수신에 실패했습니다.(sendMessage:%s, receiveMessage:%s)";
    private static final long serialVersionUID = -2241985245784088426L;

    public OrderStatusMessageStreamException() {
        super();
    }

    public OrderStatusMessageStreamException(String receiveMessage) {
        super(String.format(PAYLOAD_MESSAGE, receiveMessage));
    }

    public OrderStatusMessageStreamException(String sendMessage, String receiveMessage) {
        super(String.format(SEND_AND_RECEIVE_MESSAGE, sendMessage, receiveMessage));
    }
}
