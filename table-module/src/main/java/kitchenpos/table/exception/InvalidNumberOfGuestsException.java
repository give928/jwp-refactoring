package kitchenpos.table.exception;

public class InvalidNumberOfGuestsException extends RuntimeException {
    public static final String MESSAGE = "방문한 손님 수를 0명이상 입력해주세요.";
    private static final long serialVersionUID = -7434265510488706094L;

    public InvalidNumberOfGuestsException() {
        super(MESSAGE);
    }
}
