package kitchenpos.menu.domain;

import kitchenpos.common.domain.Name;
import kitchenpos.common.domain.Price;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Entity
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Name name;

    @Embedded
    private Price price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_menu_menu_group"))
    private MenuGroup menuGroup;

    @Embedded
    private MenuProducts menuProducts;

    protected Menu() {
    }

    private Menu(Long id, Name name, Price price, MenuGroup menuGroup, MenuProducts menuProducts) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.menuGroup = menuGroup;
        this.menuProducts = menuProducts.initMenu(this);
    }

    public static Menu of(Name name, Price price, MenuGroup menuGroup, MenuProducts menuProducts) {
        return of(null, name, price, menuGroup, menuProducts);
    }

    public static Menu of(Long id, Name name, Price price, MenuGroup menuGroup, MenuProducts menuProducts) {
        return new Menu(id, name, price, menuGroup, menuProducts);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public BigDecimal getPrice() {
        return price.get();
    }

    public MenuGroup getMenuGroup() {
        return menuGroup;
    }

    public List<MenuProduct> getMenuProducts() {
        return menuProducts.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Menu menu = (Menu) o;
        return Objects.equals(getId(), menu.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
