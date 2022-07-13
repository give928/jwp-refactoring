package kitchenpos.menu.domain;

import kitchenpos.menu.dto.ProductResponse;
import kitchenpos.menu.exception.InvalidMenuPriceException;
import kitchenpos.menu.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class MenuValidator {
    private final ProductClient productClient;

    public MenuValidator(ProductClient productClient) {
        this.productClient = productClient;
    }

    public boolean create(Menu menu) {
        validatePrice(menu);
        return true;
    }

    private void validatePrice(Menu menu) {
        if (isInvalidPrice(menu, sumPriceOfMenuProducts(menu.getMenuProducts()))) {
            throw new InvalidMenuPriceException();
        }
    }

    private boolean isInvalidPrice(Menu menu, BigDecimal menuProductsPrice) {
        return menu.getPrice().compareTo(menuProductsPrice) > 0;
    }

    private BigDecimal sumPriceOfMenuProducts(List<MenuProduct> menuProducts) {
        List<ProductResponse> productResponses = productClient.getProducts(mapProductIds(menuProducts));
        return menuProducts.stream()
                .map(menuProduct -> findProduct(productResponses, menuProduct.getProductId()).getPrice()
                        .multiply(BigDecimal.valueOf(menuProduct.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Long> mapProductIds(List<MenuProduct> menuProducts) {
        return menuProducts.stream()
                .map(MenuProduct::getProductId)
                .collect(Collectors.toList());
    }

    private ProductResponse findProduct(List<ProductResponse> productResponses, Long productId) {
        return productResponses.stream()
                .filter(productResponse -> Objects.equals(productResponse.getId(), productId))
                .findAny()
                .orElseThrow(ProductNotFoundException::new);
    }
}
