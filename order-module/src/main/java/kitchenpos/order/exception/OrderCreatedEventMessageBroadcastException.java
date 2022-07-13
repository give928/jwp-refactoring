package kitchenpos.order.exception;

import kitchenpos.order.domain.OrderCreatedEventReceivedMessage;

public class OrderCreatedEventMessageBroadcastException extends RuntimeException {
    public static final String PAYLOAD_MESSAGE = "주문 생성 이벤트 메시지 발행 중 예외가 발생했습니다.(payload:%s)";
    public static final String SEND_AND_RECEIVE_MESSAGE = "주문 생성 이벤트 메시지 송수신에 실패했습니다.(sendMessage:%s, receiveMessage:%s)";
    public static final String RECEIVE_MESSAGE = "주문 생성 이벤트 수신 메시지가 유효하지 않습니다.(orderId:%d, sentMessage:%s, receivedMessage:%s)";

    private static final long serialVersionUID = 5471065979825032844L;

    public OrderCreatedEventMessageBroadcastException(String payload) {
        super(String.format(PAYLOAD_MESSAGE, payload));
    }

    public OrderCreatedEventMessageBroadcastException(String sendMessage, String receiveMessage) {
        super(String.format(SEND_AND_RECEIVE_MESSAGE, sendMessage, receiveMessage));
    }

    public OrderCreatedEventMessageBroadcastException(OrderCreatedEventReceivedMessage message) {
        super(String.format(RECEIVE_MESSAGE, message.getOrderId(), message.getSentMessage(), message.getReceivedMessage()));
    }
}
