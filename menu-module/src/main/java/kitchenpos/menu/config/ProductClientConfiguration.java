package kitchenpos.menu.config;

import kitchenpos.menu.domain.ProductClient;
import kitchenpos.menu.domain.RestProductClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"local", "prod"})
@Configuration
public class ProductClientConfiguration {
    @Bean
    public ProductClient productClient() {
        return new RestProductClient();
    }
}
