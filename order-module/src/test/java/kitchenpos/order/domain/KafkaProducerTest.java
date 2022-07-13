package kitchenpos.order.domain;

import kitchenpos.common.SpringKafkaTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.Collections;

import static kitchenpos.order.OrderFixtures.aOrder1;
import static kitchenpos.order.OrderFixtures.aOrder2;
import static org.assertj.core.api.Assertions.assertThat;

class KafkaProducerTest extends SpringKafkaTest {
    @Autowired
    private KafkaProducer kafkaProducer;

    @DisplayName("주문 생성 이벤트 메시지를 발행하고 응답을 받는다.")
    @Test
    void broadcastOrderCreatedEvent() {
        // given
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.from(aOrder1().build());

        // when
        OrderCreatedEventReceivedMessage receiveMessage = kafkaProducer.broadcast(orderCreatedEvent);

        // then
        assertThat(receiveMessage.getReceivedMessage()).isNotNull();
    }

    @DisplayName("주문 테이블 주문 미완료 메시지를 발행한다.")
    @Test
    void broadcastOrderTableUncompleted() {
        // given
        OrderUncompletedSendingMessage orderUncompletedSendingMessage =
                OrderUncompletedSendingMessage.from(OrderTableChangedEmptyReceivedMessage.from(1L, false),
                                                    Collections.singletonList(aOrder1().build()));

        // when
        ListenableFuture<SendResult<String, String>> listenableFuture = kafkaProducer.broadcast(
                orderUncompletedSendingMessage);

        // then
        assertThat(listenableFuture).isNotNull();
        assertThat(listenableFuture.isCancelled()).isFalse();
    }

    @DisplayName("단체 지정 주문 테이블 주문 미완료 메시지를 발행한다.")
    @Test
    void broadcastOrderTablesUncompleted() {
        // given
        OrdersUncompletedSendingMessage ordersUncompletedSendingMessage =
                OrdersUncompletedSendingMessage.from(TableUngroupedReceivedMessage.from(1L, Arrays.asList(1L, 2L)),
                                                     Arrays.asList(aOrder1().build(), aOrder2().build()));

        // when
        ListenableFuture<SendResult<String, String>> listenableFuture = kafkaProducer.broadcast(
                ordersUncompletedSendingMessage);

        // then
        assertThat(listenableFuture).isNotNull();
        assertThat(listenableFuture.isCancelled()).isFalse();
    }
}
