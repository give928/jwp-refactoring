package kitchenpos.menu.domain;

import kitchenpos.common.domain.Name;
import kitchenpos.common.domain.Price;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    private Menu(Long id, String name, BigDecimal price, MenuGroup menuGroup, List<MenuProduct> menuProducts, MenuValidator menuValidator) {
        this.id = id;
        this.name = Name.from(name);
        this.price = Price.from(price);
        this.menuGroup = Objects.requireNonNull(menuGroup);
        this.menuProducts = MenuProducts.from(Optional.ofNullable(menuProducts)
                                                      .orElse(new ArrayList<>()))
                .initMenu(this);
        menuValidator.create(this);
    }

    public static Menu of(String name, BigDecimal price, MenuGroup menuGroup, List<MenuProduct> menuProducts, MenuValidator menuValidator) {
        return of(null, name, price, menuGroup, menuProducts, menuValidator);
    }

    public static Menu of(Long id, String name, BigDecimal price, MenuGroup menuGroup, List<MenuProduct> menuProducts, MenuValidator menuValidator) {
        return new Menu(id, name, price, menuGroup, menuProducts, menuValidator);
    }

    public void initMenuProducts(MenuProducts menuProducts) {
        this.menuProducts = menuProducts;
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

    public static Menu.MenuBuilder builder() {
        return new Menu.MenuBuilder();
    }

    public static class MenuBuilder {
        private Long id;
        private String name;
        private BigDecimal price;
        private MenuGroup menuGroup;
        private List<MenuProduct> menuProducts;
        private MenuValidator menuValidator;

        MenuBuilder() {
        }

        public Menu.MenuBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public Menu.MenuBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Menu.MenuBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Menu.MenuBuilder menuGroup(MenuGroup menuGroup) {
            this.menuGroup = menuGroup;
            return this;
        }

        public Menu.MenuBuilder menuProducts(List<MenuProduct> menuProducts) {
            this.menuProducts = menuProducts;
            return this;
        }

        public Menu.MenuBuilder menuValidator(MenuValidator menuValidator) {
            this.menuValidator = menuValidator;
            return this;
        }

        public Menu build() {
            return new Menu(this.id, this.name, this.price, this.menuGroup, this.menuProducts, this.menuValidator);
        }
    }
}
