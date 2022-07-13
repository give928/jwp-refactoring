package kitchenpos.order.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static kitchenpos.order.OrderFixtures.aOrder1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessageBroadcaster messageBroadcaster;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    private final Acknowledgment acknowledgment = new Acknowledgment() {
        @Override
        public void acknowledge() {
        }

        @Override
        public void nack(long sleep) {
            Acknowledgment.super.nack(sleep);
        }

        @Override
        public void nack(int index, long sleep) {
            Acknowledgment.super.nack(index, sleep);
        }
    };

    @DisplayName("주문 테이블의 빈 테이블 여부 변경 이벤트 리스너가 해당 테이블의 주문 상태를 확인해서 완료되었다면 외부 시스템에 주문 미완료 메시지를 발송하지 않는다.")
    @Test
    void orderTableChangedEmptyListener() throws JsonProcessingException {
        // given
        Order order = aOrder1().orderStatus(OrderStatus.COMPLETION).build();
        Long orderTableId = order.getOrderTableId();
        String payload = String.format("{\"orderTableId\":%d}", orderTableId);
        OrderTableChangedEmptyReceivedMessage orderTableChangedEmptyReceivedMessage = OrderTableChangedEmptyReceivedMessage.from(orderTableId, false);

        given(objectMapper.readValue(payload, OrderTableChangedEmptyReceivedMessage.class)).willReturn(
                orderTableChangedEmptyReceivedMessage);
        given(orderRepository.findByOrderTableIdAndOrderStatusIn(orderTableId, Arrays.asList(OrderStatus.COOKING,
                                                                                             OrderStatus.MEAL)))
                .willReturn(Collections.emptyList());

        // when
        kafkaConsumer.orderTableChangedEmptyListener(payload, acknowledgment);

        // then
        then(objectMapper).should(times(1))
                .readValue(payload, OrderTableChangedEmptyReceivedMessage.class);
        then(orderRepository).should(times(1))
                .findByOrderTableIdAndOrderStatusIn(orderTableId, Arrays.asList(OrderStatus.COOKING, OrderStatus.MEAL));
        then(messageBroadcaster).should(never())
                .broadcast(OrderUncompletedSendingMessage.from(orderTableChangedEmptyReceivedMessage, any()));
    }

    @DisplayName("주문 테이블의 빈 테이블 여부 변경 이벤트 리스너가 해당 테이블의 주문 상태를 확인해서 완료되지 않았다면 외부 시스템에 주문 미완료 메시지를 발송한다.")
    @Test
    void orderTableChangedEmptyListenerBroadcast() throws JsonProcessingException {
        // given
        Order order = aOrder1().build();
        Long orderTableId = order.getOrderTableId();
        String payload = String.format("{\"orderTableId\":%d}", orderTableId);
        OrderTableChangedEmptyReceivedMessage orderTableChangedEmptyReceivedMessage =
                OrderTableChangedEmptyReceivedMessage.from(orderTableId, false);
        OrderUncompletedSendingMessage orderUncompletedSendingMessage =
                OrderUncompletedSendingMessage.from(orderTableChangedEmptyReceivedMessage, Collections.singletonList(order));
        List<OrderStatus> orderStatuses = Arrays.asList(OrderStatus.COOKING, OrderStatus.MEAL);

        given(objectMapper.readValue(payload, OrderTableChangedEmptyReceivedMessage.class)).willReturn(
                orderTableChangedEmptyReceivedMessage);
        given(orderRepository.findByOrderTableIdAndOrderStatusIn(orderTableId, orderStatuses))
                .willReturn(Collections.singletonList(order));
        given(messageBroadcaster.broadcast(orderUncompletedSendingMessage))
                .willReturn(new SettableListenableFuture<>());

        // when
        kafkaConsumer.orderTableChangedEmptyListener(payload, acknowledgment);

        // then
        then(objectMapper).should(times(1))
                .readValue(payload, OrderTableChangedEmptyReceivedMessage.class);
        then(orderRepository).should(times(1))
                .findByOrderTableIdAndOrderStatusIn(orderTableId, orderStatuses);
        then(messageBroadcaster).should(times(1))
                .broadcast(orderUncompletedSendingMessage);
    }
}
