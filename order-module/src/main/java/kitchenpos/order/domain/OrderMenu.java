package kitchenpos.order.domain;

import kitchenpos.common.domain.Name;
import kitchenpos.common.domain.Price;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class OrderMenu {
    @Column(name = "menu_id", nullable = false)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "menu_name"))
    private Name name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "menu_price"))
    private Price price;

    protected OrderMenu() {
    }

    private OrderMenu(Long id) {
        this.id = id;
    }

    private OrderMenu(Long id, String name, Long price) {
        this.id = id;
        this.name = Name.from(name);
        this.price = Price.from(BigDecimal.valueOf(price));
    }

    public static OrderMenu from(Long menuId) {
        return new OrderMenu(menuId);
    }

    public static OrderMenu of(Long menuId, String name, Long price) {
        return new OrderMenu(menuId, name, price);
    }

    public Long getMenuId() {
        return id;
    }

    public Name getMenuName() {
        return name;
    }

    public Price getMenuPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderMenu orderMenu = (OrderMenu) o;
        return Objects.equals(getMenuId(), orderMenu.getMenuId())
                && Objects.equals(getMenuName(), orderMenu.getMenuName())
                && Objects.equals(getMenuPrice(), orderMenu.getMenuPrice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMenuId(), getMenuName(), getMenuPrice());
    }
}
