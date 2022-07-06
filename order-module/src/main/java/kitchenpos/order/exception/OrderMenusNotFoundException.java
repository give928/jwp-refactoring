package kitchenpos.order.exception;

public class OrderMenusNotFoundException extends RuntimeException {
    public static final String MESSAGE = "주문 메뉴를 찾을 수 없습니다.";
    private static final long serialVersionUID = -8401404445377241207L;

    public OrderMenusNotFoundException() {
        super(MESSAGE);
    }
}
