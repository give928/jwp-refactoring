package kitchenpos.common;

import org.springframework.kafka.test.context.EmbeddedKafka;

@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://${spring.kafka.bootstrap-servers}", "port=${spring.kafka.port}"})
public class SpringKafkaTest extends SpringTest {
}
