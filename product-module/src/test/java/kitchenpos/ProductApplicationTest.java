package kitchenpos;

import kitchenpos.common.SpringTest;
import kitchenpos.product.application.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ProductApplicationTest extends SpringTest {
    @Autowired
    private ProductService productService;

    @Test
    void contextLoads() {
        assertThat(productService).isNotNull();
    }
}
