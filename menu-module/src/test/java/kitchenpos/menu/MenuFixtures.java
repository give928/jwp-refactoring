package kitchenpos.menu;

import kitchenpos.menu.acceptance.MenuAcceptanceTest;
import kitchenpos.menu.domain.*;
import kitchenpos.menu.domain.Menu.MenuBuilder;
import kitchenpos.menu.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Stream;

public class MenuFixtures {
    public static ProductResponse aProductResponse1() {
        return new ProductResponse(1L, "음식1", BigDecimal.valueOf(6_000));
    }

    public static ProductResponse aProductResponse2() {
        return new ProductResponse(2L, "음식2", BigDecimal.valueOf(7_000));
    }

    public static ProductResponse aProductResponse3() {
        return new ProductResponse(3L, "음식3", BigDecimal.valueOf(8_000));
    }

    public static ProductResponse aProductResponse4() {
        return new ProductResponse(4L, "음식4", BigDecimal.valueOf(9_000));
    }

    public static MenuGroup aMenuGroup1() {
        return MenuGroup.of(1L, "메뉴그룹1");
    }

    public static MenuGroup aMenuGroup2() {
        return MenuGroup.of(2L, "메뉴그룹2");
    }

    public static MenuProduct aMenuProduct1() {
        return MenuProduct.of(1L, null, aProductResponse1().getId(), 1L);
    }

    public static MenuProduct aMenuProduct2() {
        return MenuProduct.of(2L, null, aProductResponse2().getId(), 1L);
    }

    public static MenuProduct aMenuProduct3() {
        return MenuProduct.of(3L, null, aProductResponse3().getId(), 1L);
    }

    public static MenuProducts aMenuProducts1() {
        return MenuProducts.from(Arrays.asList(aMenuProduct1(), aMenuProduct2()));
    }

    public static MenuProducts aMenuProducts2() {
        return MenuProducts.from(Arrays.asList(aMenuProduct2(), aMenuProduct3()));
    }

    public static MenuProducts aMenuProducts3() {
        return MenuProducts.from(Arrays.asList(aMenuProduct1(), aMenuProduct3()));
    }

    public static MenuBuilder aMenu1() {
        return Menu.builder()
                .id(1L)
                .name("메뉴1")
                .price(Stream.of(aProductResponse1(), aProductResponse2())
                               .map(menuProduct -> menuProduct.getPrice().multiply(BigDecimal.valueOf(1)))
                               .reduce(BigDecimal.ZERO, BigDecimal::add))
                .menuGroup(aMenuGroup1())
                .menuProducts(aMenuProducts1().get())
                .menuValidator(aMenuValidator());
    }

    public static MenuBuilder aMenu2() {
        return Menu.builder()
                .id(2L)
                .name("메뉴2")
                .price(Stream.of(aProductResponse2(), aProductResponse3())
                               .map(menuProduct -> menuProduct.getPrice().multiply(BigDecimal.valueOf(1)))
                               .reduce(BigDecimal.ZERO, BigDecimal::add))
                .menuGroup(aMenuGroup1())
                .menuProducts(aMenuProducts2().get())
                .menuValidator(aMenuValidator());
    }

    public static MenuBuilder aMenu3() {
        return Menu.builder()
                .id(3L)
                .name("메뉴3")
                .price(Stream.of(aProductResponse1(), aProductResponse3())
                               .map(menuProduct -> menuProduct.getPrice().multiply(BigDecimal.valueOf(1)))
                               .reduce(BigDecimal.ZERO, BigDecimal::add))
                .menuGroup(aMenuGroup1())
                .menuProducts(aMenuProducts3().get())
                .menuValidator(aMenuValidator());
    }

    public static MenuValidator aMenuValidator() {
        return new MenuValidator(new MenuAcceptanceTest.TestProductClient(
                Arrays.asList(aProductResponse1(), aProductResponse2(), aProductResponse3(), aProductResponse4()))) {
            @Override
            public boolean create(Menu menu) {
                return true;
            }
        };
    }
}
