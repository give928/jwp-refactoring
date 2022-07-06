package kitchenpos.menu.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.menu.exception.MenusMessageStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class MenuKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(MenuKafkaConsumer.class);

    private static final String MENU_IDS_PROPERTY_NAME = "menuIds";
    private static final String MENU_ID_PROPERTY_NAME = "menuId";
    private static final String REPLY_EXISTS_MENUS_MESSAGE_FORMAT = "{\"menuIds\":%s,\"exists\":%s}";

    private final ObjectMapper objectMapper;
    private final MenuRepository menuRepository;

    public MenuKafkaConsumer(final ObjectMapper objectMapper, final MenuRepository menuRepository) {
        this.objectMapper = objectMapper;
        this.menuRepository = menuRepository;
    }

    @KafkaListener(topics = "${kafka.topics.exists-menus}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String existsMenusListener(@Payload String payload, Acknowledgment acknowledgment) {
        try {
            log.info("MenuKafkaConsumer.existsMenusListener payload:{}", payload);
            JsonNode jsonNode = objectMapper.readTree(payload);
            List<Long> menuIds = mapMenuIds(jsonNode);
            String replyMessage = String.format(REPLY_EXISTS_MENUS_MESSAGE_FORMAT,
                                                jsonNode.get(MENU_IDS_PROPERTY_NAME).toString(),
                                                existsMenus(menuIds));
            log.info("MenuKafkaConsumer.existsMenusListener replyMessage:{}", replyMessage);
            acknowledgment.acknowledge();
            return replyMessage;
        } catch (JsonProcessingException e) {
            throw new MenusMessageStreamException(payload);
        }
    }

    private List<Long> mapMenuIds(JsonNode jsonNode) {
        return StreamSupport.stream(jsonNode
                                     .get(MENU_IDS_PROPERTY_NAME)
                                     .spliterator(),
                             false)
                .map(j -> j.get(MENU_ID_PROPERTY_NAME).asLong())
                .collect(Collectors.toList());
    }

    private boolean existsMenus(List<Long> menuIds) {
        return menuRepository.countByIdIn(menuIds) == menuIds.size();
    }
}
