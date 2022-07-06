package kitchenpos.menu.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Embeddable
public class MenuProducts {
    @OneToMany(mappedBy = "menu", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<MenuProduct> values;

    protected MenuProducts() {
    }

    private MenuProducts(List<MenuProduct> values) {
        this.values = Objects.requireNonNull(values);
    }

    public static MenuProducts from(List<MenuProduct> values) {
        return new MenuProducts(values);
    }

    public MenuProducts initMenu(Menu menu) {
        validatePrice(menu.getPrice());
        values.forEach(menuProduct -> menuProduct.initMenu(menu));
        menu.initMenuProducts(this);
        return this;
    }

    private void validatePrice(BigDecimal price) {
        if (price.compareTo(sumPrice()) > 0) {
            throw new IllegalArgumentException();
        }
    }

    private BigDecimal sumPrice() {
        return values.stream()
                .map(menuProduct -> menuProduct.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(menuProduct.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<MenuProduct> get() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MenuProducts that = (MenuProducts) o;
        return Objects.equals(get(), that.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get());
    }
}
