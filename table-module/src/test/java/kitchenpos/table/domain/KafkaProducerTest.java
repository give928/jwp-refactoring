package kitchenpos.table.domain;

import kitchenpos.common.SpringKafkaTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import static kitchenpos.table.TableFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class KafkaProducerTest extends SpringKafkaTest {
    @Autowired
    private KafkaProducer kafkaProducer;

    @DisplayName("주문 테이블 빈 테이블 여부 변경 이벤트 메시지를 발행한다.")
    @Test
    void sendChangeEmptyOrderTable() {
        // given
        OrderTableChangedEmptyEvent event =
                OrderTableChangedEmptyEvent.from(aOrderTable1().changeEmpty(aOrderTableValidator(), false));

        // when
        ListenableFuture<SendResult<String, String>> listenableFuture = kafkaProducer.broadcast(event);

        // then
        assertThat(listenableFuture).isNotNull();
        assertThat(listenableFuture.isCancelled()).isFalse();
    }

    @DisplayName("단체 지정 이벤트 메시지를 발행한다.")
    @Test
    void sendGroupTable() {
        // given
        TableUngroupedEvent event = TableUngroupedEvent.from(aTableGroup1().ungroup());

        // when
        ListenableFuture<SendResult<String, String>> listenableFuture = kafkaProducer.broadcast(event);

        // then
        assertThat(listenableFuture).isNotNull();
        assertThat(listenableFuture.isCancelled()).isFalse();
    }
}
