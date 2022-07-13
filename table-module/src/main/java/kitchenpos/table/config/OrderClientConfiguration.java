package kitchenpos.table.config;

import kitchenpos.table.domain.OrderClient;
import kitchenpos.table.domain.RestOrderClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"local", "prod"})
@Configuration
public class OrderClientConfiguration {
    @Bean
    public OrderClient menuClient() {
        return new RestOrderClient();
    }
}
