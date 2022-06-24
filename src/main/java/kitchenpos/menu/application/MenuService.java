package kitchenpos.menu.application;

import kitchenpos.common.domain.Price;
import kitchenpos.menu.domain.*;
import kitchenpos.menu.dto.MenuProductRequest;
import kitchenpos.menu.dto.MenuRequest;
import kitchenpos.menu.dto.MenuResponse;
import kitchenpos.product.domain.Product;
import kitchenpos.product.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final ProductRepository productRepository;

    public MenuService(final MenuRepository menuRepository, final MenuGroupRepository menuGroupRepository,
                       final ProductRepository productRepository) {
        this.menuRepository = menuRepository;
        this.menuGroupRepository = menuGroupRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public MenuResponse create(final MenuRequest menuRequest) {
        MenuGroup menuGroup = menuGroupRepository.findById(menuRequest.getMenuGroupId())
                .orElseThrow(IllegalArgumentException::new);
        List<MenuProduct> menuProducts = mapMenuProducts(menuRequest.getMenuProducts());

        Menu menu = Menu.of(menuRequest.getName(), Price.from(menuRequest.getPrice()), menuGroup, MenuProducts.from(menuProducts));
        return MenuResponse.from(menuRepository.save(menu));
    }

    private List<MenuProduct> mapMenuProducts(List<MenuProductRequest> menuProductRequests) {
        List<Product> products = productRepository.findByIdIn(mapProductIds(menuProductRequests));
        return menuProductRequests.stream()
                .map(menuProductRequest -> MenuProduct.of(findProduct(products, menuProductRequest.getProductId()),
                                                          menuProductRequest.getQuantity()))
                .collect(Collectors.toList());
    }

    private List<Long> mapProductIds(List<MenuProductRequest> menuProductRequests) {
        return menuProductRequests.stream()
                .map(MenuProductRequest::getProductId)
                .collect(Collectors.toList());
    }

    private Product findProduct(List<Product> products, Long productId) {
        return products.stream()
                .filter(product -> Objects.equals(product.getId(), productId))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    public List<MenuResponse> list() {
        return menuRepository.findAll().stream()
                .map(MenuResponse::from)
                .collect(Collectors.toList());
    }
}
