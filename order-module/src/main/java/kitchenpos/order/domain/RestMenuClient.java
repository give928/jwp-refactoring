package kitchenpos.order.domain;

import kitchenpos.common.util.RestUtils;
import kitchenpos.order.dto.MenuResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Component
public class RestMenuClient implements MenuClient {
    @Value("${module.menu.url}")
    private String menuUrl;

    @Value("${module.menu.list.path}")
    private String menuListPath;

    @Override
    public List<MenuResponse> getMenus(List<Long> menuIds) {
        ResponseEntity<List<MenuResponse>> responseEntity = RestUtils.get(menuUrl, menuListPath,
                                                                          mapMenuIdsParams(menuIds),
                                                                          new ParameterizedTypeReference<>() {
                                                                          });
        return responseEntity.getBody();
    }

    private MultiValueMap<String, String> mapMenuIdsParams(List<Long> menuIds) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        menuIds.forEach(menuId -> parameters.add("id", String.valueOf(menuId)));
        return parameters;
    }
}
