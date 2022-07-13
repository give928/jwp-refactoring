package kitchenpos.common.exception;

public class KafkaMessageSendException extends RuntimeException {
    public static final String MESSAGE = "카프카 메시지 전송에 실패했습니다.(send message: %s, throwable: %s)";
    private static final long serialVersionUID = 8785424442331442779L;

    public KafkaMessageSendException(String sendMessage, Throwable throwable) {
        super(String.format(MESSAGE, sendMessage, throwable.getMessage()));
    }
}
