package kitchenpos.menu.domain;

import kitchenpos.common.util.RestUtils;
import kitchenpos.menu.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Component
public class RestProductClient implements ProductClient {
    @Value("${module.product.url}")
    private String productUrl;

    @Value("${module.product.findProductsByIdIn.path}")
    private String findProductsByIdInPath;

    @Override
    public List<ProductResponse> getProducts(List<Long> productIds) {
        ResponseEntity<List<ProductResponse>> responseEntity = RestUtils.get(productUrl, findProductsByIdInPath,
                                                                             mapProductIdsParams(productIds),
                                                                             new ParameterizedTypeReference<>() {});
        return responseEntity.getBody();
    }

    private MultiValueMap<String, String> mapProductIdsParams(List<Long> productIds) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        productIds.forEach(productId -> parameters.add("id", String.valueOf(productId)));
        return parameters;
    }
}
