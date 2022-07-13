package kitchenpos.order.config;

import kitchenpos.order.domain.MenuClient;
import kitchenpos.order.domain.RestMenuClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"local", "prod"})
@Configuration
public class MenuClientConfiguration {
    @Bean
    public MenuClient menuClient() {
        return new RestMenuClient();
    }
}
