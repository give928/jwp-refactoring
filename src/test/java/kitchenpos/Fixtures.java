package kitchenpos;

import kitchenpos.menu.domain.*;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderLineItems;
import kitchenpos.order.domain.OrderValidator;
import kitchenpos.product.domain.Product;
import kitchenpos.table.domain.*;

import java.math.BigDecimal;
import java.util.Arrays;

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

    public static Menu aMenu1() {
        return Menu.of(1L, "메뉴1", BigDecimal.valueOf(3), aMenuGroup1(), aMenuProducts1().get(), aMenuValidator());
    }

    public static Menu aMenu2() {
        return Menu.of(2L, "메뉴2", BigDecimal.valueOf(5), aMenuGroup1(), aMenuProducts2().get(), aMenuValidator());
    }

    public static Menu aMenu3() {
        return Menu.of(3L, "메뉴3", BigDecimal.valueOf(4), aMenuGroup1(), aMenuProducts3().get(), aMenuValidator());
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
        return new OrderTableValidator(null) {
            @Override
            public boolean clearTableGroup(OrderTable orderTable) {
                return true;
            }

            @Override
            public boolean changeEmpty(OrderTable orderTable) {
                return true;
            }
        };
    }

    public static OrderTableValidator aOrderTableValidatorThrownByClearTableGroup() {
        return new OrderTableValidator(null) {
            @Override
            public boolean clearTableGroup(OrderTable orderTable) {
                throw new IllegalArgumentException();
            }

            @Override
            public boolean changeEmpty(OrderTable orderTable) {
                return true;
            }
        };
    }

    public static OrderTableValidator aOrderTableValidatorThrownByChangeEmpty() {
        return new OrderTableValidator(null) {
            @Override
            public boolean clearTableGroup(OrderTable orderTable) {
                return true;
            }

            @Override
            public boolean changeEmpty(OrderTable orderTable) {
                throw new IllegalArgumentException();
            }
        };
    }

    public static TableGroupValidator aTableGroupValidator() {
        return aTableGroupValidator(aOrderTableValidator());
    }

    public static TableGroupValidator aTableGroupValidator(OrderTableValidator orderTableValidator) {
        return new TableGroupValidator(orderTableValidator) {
            @Override
            public boolean clearTableGroup(OrderTable orderTable) {
                return true;
            }

            @Override
            public boolean changeEmpty(OrderTable orderTable) {
                return true;
            }
        };
    }

    public static OrderLineItem aOrderLineItem1() {
        return OrderLineItem.of(1L, null, aMenu1().getId(), 1);
    }

    public static OrderLineItem aOrderLineItem2() {
        return OrderLineItem.of(2L, null, aMenu2().getId(), 1);
    }

    public static OrderLineItem aOrderLineItem3() {
        return OrderLineItem.of(3L, null, aMenu3().getId(), 1);
    }

    public static OrderLineItems aOrderLineItems1() {
        return OrderLineItems.from(Arrays.asList(aOrderLineItem1(), aOrderLineItem2()));
    }

    public static OrderLineItems aOrderLineItems2() {
        return OrderLineItems.from(Arrays.asList(aOrderLineItem2(), aOrderLineItem3()));
    }

    public static Order aOrder1() {
        return Order.of(1L, aOrderTable1().getId(), aOrderLineItems1().get(), aOrderValidator());
    }

    public static Order aOrder2() {
        return Order.of(2L, aOrderTable2().getId(), aOrderLineItems2().get(), aOrderValidator());
    }

    public static OrderValidator aOrderValidator() {
        return new OrderValidator(null, null) {
            @Override
            public boolean create(Order order) {
                return true;
            }
        };
    }

    public static OrderValidator aOrderValidatorThrownByCreate() {
        return new OrderValidator(null, null) {
            @Override
            public boolean create(Order order) {
                throw new IllegalArgumentException();
            }
        };
    }
}
