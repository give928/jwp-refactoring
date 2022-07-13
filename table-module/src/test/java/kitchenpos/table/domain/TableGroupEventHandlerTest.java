package kitchenpos.table.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static kitchenpos.table.TableFixtures.aTableGroup1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TableGroupEventHandlerTest {
    @Mock
    private MessageBroadcaster messageBroadcaster;

    @InjectMocks
    private TableGroupEventHandler tableGroupEventHandler;

    @DisplayName("단체 지정 해제 이벤트 핸들러가 외부 시스템에 메시지를 발행한다.")
    @Test
    void handle() {
        // given
        TableUngroupedEvent event = TableUngroupedEvent.from(aTableGroup1());

        given(messageBroadcaster.broadcast(event)).willReturn(any());

        // when
        tableGroupEventHandler.handle(event);

        // then
        then(messageBroadcaster).should(times(1)).broadcast(event);
    }
}
