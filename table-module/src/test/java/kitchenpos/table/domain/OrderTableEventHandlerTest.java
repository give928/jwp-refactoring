package kitchenpos.table.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static kitchenpos.table.TableFixtures.aOrderTable1;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OrderTableEventHandlerTest {
    @Mock
    private MessageBroadcaster messageBroadcaster;

    @InjectMocks
    private OrderTableEventHandler orderTableEventHandler;

    @DisplayName("주문 테이블 빈 테이블 여부 변경 이벤트 핸들러가 외부 시스템에 메시지를 발행한다.")
    @Test
    void handle() {
        // given
        OrderTableChangedEmptyEvent event = OrderTableChangedEmptyEvent.from(aOrderTable1());

        given(messageBroadcaster.broadcast(event)).willReturn(any());

        // when
        orderTableEventHandler.handle(event);

        // then
        then(messageBroadcaster).should(times(1)).broadcast(event);
    }
}
