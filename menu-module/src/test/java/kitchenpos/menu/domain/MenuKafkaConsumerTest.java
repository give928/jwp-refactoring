package kitchenpos.menu.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static kitchenpos.menu.MenuFixtures.aMenu1;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://${spring.kafka.bootstrap-servers}", "port=${spring.kafka.port}"})
class MenuKafkaConsumerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MenuGroupRepository menuGroupRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuKafkaConsumer menuKafkaConsumer;

    @DisplayName("메뉴 확인 메시지 스트림 리스너가 전체 메뉴들의 존재 여부를 반환한다.")
    @Test
    void getMenusListener() throws JsonProcessingException {
        // given
        MenuGroup menuGroup = menuGroupRepository.save(MenuGroup.of("메뉴그룹1"));
        Menu menu = menuRepository.save(aMenu1().id(null)
                                                .menuGroup(menuGroup)
                                                .menuProducts(Arrays.asList(MenuProduct.of(1L, 1),
                                                                            MenuProduct.of(2L, 2)))
                                                .build());
        String payload = String.format("{\"menuIds\":[%s]}", String.format("{\"menuId\":%d}", menu.getId()));

        // when
        String replyMessage = menuKafkaConsumer.getMenusListener(payload, acknowledgment);

        // then
        JsonNode jsonNode = objectMapper.readTree(replyMessage);
        assertThat(jsonNode.get("menuIds").get(0).get("menuId").asLong()).isEqualTo(menu.getId());
        assertThat(jsonNode.get("menus").get(0).get("id").asLong()).isPositive();
    }

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
}
