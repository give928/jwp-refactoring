package kitchenpos.menu.domain;

import kitchenpos.menu.dto.ProductResponse;

import java.util.List;

public interface ProductClient {
    List<ProductResponse> getProducts(List<Long> productIds);
}
