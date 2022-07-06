package kitchenpos;

import kitchenpos.product.application.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductApplicationTest {
    @Autowired
    private ProductService productService;

    @Test
    void contextLoads() {
        assertThat(productService).isNotNull();
    }
}
