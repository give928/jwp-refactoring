package kitchenpos.product;

import kitchenpos.product.domain.Product;

import java.math.BigDecimal;

public class ProductFixtures {
    public static Product aProduct1() {
        return Product.of(1L, "음식1", BigDecimal.ONE);
    }

    public static Product aProduct2() {
        return Product.of(2L, "음식2", BigDecimal.valueOf(2));
    }

    public static Product aProduct3() {
        return Product.of(3L, "음식3", BigDecimal.valueOf(3));
    }
}
