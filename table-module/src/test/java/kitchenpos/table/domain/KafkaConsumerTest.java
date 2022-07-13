package kitchenpos.table.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import kitchenpos.common.SpringKafkaTest;
import kitchenpos.table.exception.OrderTableNotFoundException;
import kitchenpos.table.exception.TableGroupNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaConsumerTest extends SpringKafkaTest {
    public static OrderTable orderTable;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrderTableRepository orderTableRepository;

    @Autowired
    private TableGroupRepository tableGroupRepository;

    @Autowired
    private TableGroupValidator tableGroupValidator;

    @Autowired
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

    @DisplayName("주문 생성 이벤트 메시지 스트림 리스너가 해당 주문 테이블의 상태를 반환한다.")
    @Test
    @Transactional
    void orderCreatedListener() throws JsonProcessingException {
        // given
        Long orderId = 1L;
        orderTable = orderTableRepository.save(OrderTable.of(0, true));
        entityManager.flush();
        entityManager.clear();
        Optional<OrderTable> optionalOrderTable = orderTableRepository.findById(orderTable.getId());
        String payload = String.format("{\"orderId\":%d,\"orderTableId\":%d}", orderId, orderTable.getId());

        // when
        String replyMessage = kafkaConsumer.orderCreatedListener(payload, acknowledgment);

        // then
        assertThat(replyMessage).isEqualTo(
                String.format(KafkaConsumer.REPLY_ORDER_CREATED_MESSAGE_FORMAT, orderId, orderTable.getId(),
                              optionalOrderTable.isPresent(), optionalOrderTable.filter(OrderTable::isEmpty)
                                      .isPresent()));
    }

    @DisplayName("주문 미완료 메시지 스트림 리스너가 주문 테이블의 빈 테이블 여부 변경을 되돌린다.")
    @Test
    @Transactional
    void orderUncompletedListener() {
        // given
        Long orderId = 1L;
        boolean empty = true;
        OrderTable orderTable = orderTableRepository.save(OrderTable.of(0, empty));
        entityManager.flush();
        entityManager.clear();
        String payload = String.format("{\"orderTableId\":%d,\"empty\":%s,\"orderIds\":[%d]}", orderTable.getId(),
                                       empty, orderId);

        // when
        kafkaConsumer.orderUncompletedListener(payload, acknowledgment);

        entityManager.flush();
        entityManager.clear();

        OrderTable findOrderTable = orderTableRepository.findById(orderTable.getId())
                .orElseThrow(OrderTableNotFoundException::new);

        // then
        assertThat(findOrderTable.isEmpty()).isEqualTo(!empty);
    }

    @DisplayName("주문 미완료 메시지 스트림 리스너가 단체 지정 해제를 되돌린다.")
    @Test
    @Transactional
    void ordersUncompletedListener() {
        // given
        Long orderId = 1L;
        OrderTable orderTable1 = orderTableRepository.save(OrderTable.of(0, true));
        OrderTable orderTable2 = orderTableRepository.save(OrderTable.of(0, true));
        TableGroup tableGroup = tableGroupRepository.save(
                TableGroup.of(new ArrayList<>(Arrays.asList(orderTable1, orderTable2)), tableGroupValidator));
        tableGroup.ungroup();
        entityManager.flush();
        entityManager.clear();
        String payload = String.format("{\"tableGroupId\":%d,\"orderTableIds\":%s,\"orderIds\":[%d]}",
                                       tableGroup.getId(),
                                       String.format("[%d,%d]", orderTable1.getId(), orderTable2.getId()), orderId);

        // when
        kafkaConsumer.ordersUncompletedListener(payload, acknowledgment);

        entityManager.flush();
        entityManager.clear();

        TableGroup findTableGroup = tableGroupRepository.findById(tableGroup.getId())
                .orElseThrow(TableGroupNotFoundException::new);

        // then
        assertThat(findTableGroup.getId()).isEqualTo(tableGroup.getId());
        assertThat(findTableGroup.getOrderTables()
                           .stream()
                           .map(OrderTable::getId)
                           .sorted()
                           .collect(Collectors.toList()))
                .isEqualTo(Arrays.asList(orderTable1.getId(), orderTable2.getId()));
    }
}
