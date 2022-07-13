package kitchenpos.menu.application;

import kitchenpos.menu.domain.*;
import kitchenpos.menu.dto.MenuProductRequest;
import kitchenpos.menu.dto.MenuRequest;
import kitchenpos.menu.dto.MenuResponse;
import kitchenpos.menu.exception.MenuGroupNotFoundException;
import kitchenpos.menu.exception.RequiredMenuGroupException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final MenuValidator menuValidator;

    public MenuService(final MenuRepository menuRepository, final MenuGroupRepository menuGroupRepository,
                       final MenuValidator menuValidator) {
        this.menuRepository = menuRepository;
        this.menuGroupRepository = menuGroupRepository;
        this.menuValidator = menuValidator;
    }

    @Transactional
    public MenuResponse create(final MenuRequest menuRequest) {
        MenuGroup menuGroup = menuGroupRepository.findById(Optional.ofNullable(menuRequest.getMenuGroupId())
                                                                   .orElseThrow(RequiredMenuGroupException::new))
                .orElseThrow(MenuGroupNotFoundException::new);
        List<MenuProduct> menuProducts = mapMenuProducts(menuRequest.getMenuProducts());

        Menu menu = Menu.of(menuRequest.getName(), menuRequest.getPrice(), menuGroup, menuProducts, menuValidator);
        return MenuResponse.from(menuRepository.save(menu));
    }

    private List<MenuProduct> mapMenuProducts(List<MenuProductRequest> menuProductRequests) {
        return menuProductRequests.stream()
                .map(menuProductRequest -> MenuProduct.of(menuProductRequest.getProductId(),
                                                          menuProductRequest.getQuantity()))
                .collect(Collectors.toList());
    }

    public List<MenuResponse> list(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return list();
        }
        return listByIdIn(ids);
    }

    private List<MenuResponse> list() {
        return menuRepository.findAll()
                .stream()
                .map(MenuResponse::from)
                .collect(Collectors.toList());
    }

    private List<MenuResponse> listByIdIn(List<Long> ids) {
        return menuRepository.findByIdIn(ids)
                .stream()
                .map(MenuResponse::from)
                .collect(Collectors.toList());
    }
}
