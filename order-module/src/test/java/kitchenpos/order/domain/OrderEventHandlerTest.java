package kitchenpos.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static kitchenpos.order.OrderFixtures.aOrder1;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {
    @Mock
    private MessageBroadcaster messageBroadcaster;

    @Mock
    private OrderValidator orderValidator;

    @InjectMocks
    private OrderEventHandler orderEventHandler;

    @DisplayName("주문 생성 이벤트 핸들러가 외부 시스템에 메시지를 발행하고 수신 메시지로 주문 유효성을 확인한다.")
    @Test
    void handle() {
        // given
        Order order = aOrder1().build();
        OrderCreatedEvent event = OrderCreatedEvent.from(order);
        OrderCreatedEventReceivedMessage eventReceivedMessage =
                new OrderCreatedEventReceivedMessage(event.getOrderId(), order.getOrderTableId(), true, false, "sent message", "received message");

        given(messageBroadcaster.broadcast(event)).willReturn(eventReceivedMessage);
        given(orderValidator.created(eventReceivedMessage)).willReturn(true);

        // when
        orderEventHandler.handle(event);

        // then
        then(messageBroadcaster).should(times(1)).broadcast(event);
        then(orderValidator).should(times(1)).created(eventReceivedMessage);
    }
}
