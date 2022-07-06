package kitchenpos;

import kitchenpos.menu.domain.Menu.MenuBuilder;
import kitchenpos.menu.domain.MenuValidator;
import kitchenpos.menu.domain.*;
import kitchenpos.order.domain.*;
import kitchenpos.order.domain.Order.OrderBuilder;
import kitchenpos.product.domain.Product;
import kitchenpos.table.domain.OrderTableValidator;
import kitchenpos.table.domain.TableGroupValidator;
import kitchenpos.table.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Fixtures {
    public static Product aProduct1() {
        return Product.of(1L, "음식1", BigDecimal.ONE);
    }

    public static Product aProduct2() {
        return Product.of(2L, "음식2", BigDecimal.valueOf(2));
    }

    public static Product aProduct3() {
        return Product.of(3L, "음식3", BigDecimal.valueOf(3));
    }

    public static MenuGroup aMenuGroup1() {
        return MenuGroup.of(1L, "메뉴그룹1");
    }

    public static MenuGroup aMenuGroup2() {
        return MenuGroup.of(2L, "메뉴그룹2");
    }

    public static MenuProduct aMenuProduct1() {
        return MenuProduct.of(1L, null, aProduct1().getId(), 1L);
    }

    public static MenuProduct aMenuProduct2() {
        return MenuProduct.of(2L, null, aProduct2().getId(), 1L);
    }

    public static MenuProduct aMenuProduct3() {
        return MenuProduct.of(3L, null, aProduct3().getId(), 1L);
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
                .price(BigDecimal.valueOf(3))
                .menuGroup(aMenuGroup1())
                .menuProducts(aMenuProducts1().get())
                .menuValidator(aMenuValidator());
    }

    public static MenuBuilder aMenu2() {
        return Menu.builder()
                .id(2L)
                .name("메뉴2")
                .price(BigDecimal.valueOf(5))
                .menuGroup(aMenuGroup1())
                .menuProducts(aMenuProducts2().get())
                .menuValidator(aMenuValidator());
    }

    public static MenuBuilder aMenu3() {
        return Menu.builder()
                .id(3L)
                .name("메뉴3")
                .price(BigDecimal.valueOf(4))
                .menuGroup(aMenuGroup1())
                .menuProducts(aMenuProducts3().get())
                .menuValidator(aMenuValidator());
    }

    public static MenuValidator aMenuValidator() {
        return new MenuValidator(null) {
            @Override
            public boolean create(Menu menu) {
                return true;
            }
        };
    }

    public static OrderTable.OrderTableBuilder aOrderTable() {
        return OrderTable.builder()
                .id(1L)
                .tableGroup(null)
                .numberOfGuests(0)
                .empty(true);
    }

    public static OrderTable aOrderTable1() {
        return OrderTable.of(1L, null, 0, true);
    }

    public static OrderTable aOrderTable2() {
        return OrderTable.of(2L, null, 0, true);
    }

    public static OrderTables aOrderTables1() {
        return OrderTables.of(Arrays.asList(aOrderTable1(), aOrderTable2()), aTableGroupValidator());
    }

    public static TableGroup aTableGroup1() {
        return TableGroup.of(1L, aOrderTables1().get(), aTableGroupValidator());
    }

    public static OrderTableValidator aOrderTableValidator() {
        return new OrderTableValidator() {
            @Override
            public boolean changeEmpty(OrderTable orderTable) {
                return true;
            }
        };
    }

    public static TableGroupValidator aTableGroupValidator() {
        return aTableGroupValidator(aOrderTableValidator());
    }

    public static TableGroupValidator aTableGroupValidator(OrderTableValidator orderTableValidator) {
        return new TableGroupValidator(orderTableValidator) {
            @Override
            public boolean changeEmpty(OrderTable orderTable) {
                return true;
            }
        };
    }

    public static OrderLineItem aOrderLineItem1() {
        return OrderLineItem.of(1L, null, aMenu1().build().getId(), 1);
    }

    public static OrderLineItem aOrderLineItem2() {
        return OrderLineItem.of(2L, null, aMenu2().build().getId(), 1);
    }

    public static OrderLineItem aOrderLineItem3() {
        return OrderLineItem.of(3L, null, aMenu3().build().getId(), 1);
    }

    public static OrderLineItems aOrderLineItems1() {
        return OrderLineItems.from(Arrays.asList(aOrderLineItem1(), aOrderLineItem2()));
    }

    public static OrderLineItems aOrderLineItems2() {
        return OrderLineItems.from(Arrays.asList(aOrderLineItem2(), aOrderLineItem3()));
    }

    public static OrderBuilder aOrder1() {
        return Order.builder()
                .id(1L)
                .orderTableId(aOrderTable1().getId())
                .orderStatus(OrderStatus.COOKING)
                .orderedTime(LocalDateTime.now())
                .orderLineItems(aOrderLineItems1().get())
                .orderValidator(aOrderValidator());
    }

    public static OrderBuilder aOrder2() {
        return Order.builder()
                .id(2L)
                .orderTableId(aOrderTable2().getId())
                .orderStatus(OrderStatus.COOKING)
                .orderedTime(LocalDateTime.now())
                .orderLineItems(aOrderLineItems2().get())
                .orderValidator(aOrderValidator());
    }

    public static OrderValidator aOrderValidator() {
        return new OrderValidator(null, null) {
            @Override
            public boolean create(Order order) {
                return true;
            }
        };
    }

    public static List<OrderStatus> aNotCompletionOrderStatuses() {
        return Arrays.asList(OrderStatus.COOKING, OrderStatus.MEAL);
    }
}
