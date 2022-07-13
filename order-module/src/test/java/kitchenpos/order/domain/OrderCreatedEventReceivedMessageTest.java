package kitchenpos.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCreatedEventReceivedMessageTest {
    @DisplayName("주문 생성 이벤트 수신 메시지 도메인을 생성한다.")
    @Test
    void create() {
        // given
        String sentMessage = "sent message";
        String receivedMessage = "received message";

        // when
        OrderCreatedEventReceivedMessage orderCreatedEventReceivedMessage =
                new OrderCreatedEventReceivedMessage(1L, 1L, true, false, null, null);
        orderCreatedEventReceivedMessage.init(sentMessage, receivedMessage);

        // then
        assertThat(orderCreatedEventReceivedMessage.getOrderId()).isEqualTo(1L);
        assertThat(orderCreatedEventReceivedMessage.getSentMessage()).isEqualTo(sentMessage);
        assertThat(orderCreatedEventReceivedMessage.getReceivedMessage()).isEqualTo(receivedMessage);
    }
}
