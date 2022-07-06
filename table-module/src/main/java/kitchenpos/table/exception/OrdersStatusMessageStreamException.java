package kitchenpos.table.exception;

public class OrdersStatusMessageStreamException extends RuntimeException {
    public static final String PAYLOAD_MESSAGE = "단체 지정 이벤트 메시지에 전송할 주문 테이블 정보를 찾을 수 없습니다.(payload:%s)";
    public static final String SEND_AND_RECEIVE_MESSAGE = "단체 지정 이벤트 메시지 송수신에 실패했습니다.(sendMessage:%s, receiveMessage:%s)";
    private static final long serialVersionUID = -2241985245784088426L;

    public OrdersStatusMessageStreamException(String receiveMessage) {
        super(String.format(PAYLOAD_MESSAGE, receiveMessage));
    }

    public OrdersStatusMessageStreamException(String sendMessage, String receiveMessage) {
        super(String.format(SEND_AND_RECEIVE_MESSAGE, sendMessage, receiveMessage));
    }
}
