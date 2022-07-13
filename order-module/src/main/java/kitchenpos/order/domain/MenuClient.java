package kitchenpos.order.domain;

import kitchenpos.order.dto.MenuResponse;

import java.util.List;

public interface MenuClient {
    List<MenuResponse> getMenus(List<Long> menuIds);
}
