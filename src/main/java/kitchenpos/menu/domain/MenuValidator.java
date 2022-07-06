package kitchenpos.menu.domain;

import kitchenpos.menu.exception.InvalidMenuPriceException;
import kitchenpos.product.domain.Product;
import kitchenpos.product.domain.ProductRepository;
import kitchenpos.product.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class MenuValidator {
    private final ProductRepository productRepository;

    public MenuValidator(final ProductRepository productRepository) {
        this.productRepository = productRepository;
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
        List<Product> products = productRepository.findByIdIn(mapProductIds(menuProducts));
        return menuProducts.stream()
                .map(menuProduct -> findProduct(products, menuProduct.getProductId()).getPrice()
                        .multiply(BigDecimal.valueOf(menuProduct.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Long> mapProductIds(List<MenuProduct> menuProducts) {
        return menuProducts.stream()
                .map(MenuProduct::getProductId)
                .collect(Collectors.toList());
    }

    private Product findProduct(List<Product> products, Long productId) {
        return products.stream()
                .filter(product -> Objects.equals(product.getId(), productId))
                .findAny()
                .orElseThrow(ProductNotFoundException::new);
    }
}
