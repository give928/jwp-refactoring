package kitchenpos.common.exception;

public class KafkaMessageReceiveException extends RuntimeException {
    public static final String MESSAGE = "카프카 메시지 수신에 실패했습니다.(send message: %s, throwable: %s)";
    private static final long serialVersionUID = 2840953723252519512L;

    public KafkaMessageReceiveException(String sendMessage) {
        super(String.format(MESSAGE, sendMessage, "KafkaMessageReceiveException"));
    }

    public KafkaMessageReceiveException(String sendMessage, Throwable throwable) {
        super(String.format(MESSAGE, sendMessage, throwable.getMessage()));
    }
}
