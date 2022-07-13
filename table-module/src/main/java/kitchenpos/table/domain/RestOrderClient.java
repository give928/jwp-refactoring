package kitchenpos.table.domain;

import kitchenpos.common.util.RestUtils;
import kitchenpos.table.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RestOrderClient implements OrderClient {
    @Value("${module.order.url}")
    private String orderUrl;

    @Value("${module.order.find.path}")
    private String orderListPath;

    @Override
    public OrderResponse getOrder(Long orderId) {
        ResponseEntity<OrderResponse> responseEntity = RestUtils.get(StringUtils.replace(orderUrl, "{id}", String.valueOf(orderId)), orderListPath,
                                                                     new ParameterizedTypeReference<>() {
                                                                     });
        return responseEntity.getBody();
    }
}
