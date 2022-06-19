package kitchenpos.application;

import kitchenpos.dao.MenuDao;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderLineItemDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.*;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private MenuDao menuDao;
    @Mock
    private OrderDao orderDao;
    @Mock
    private OrderLineItemDao orderLineItemDao;
    @Mock
    private OrderTableDao orderTableDao;

    @InjectMocks
    private OrderService orderService;

    private Menu menu1;
    private Menu menu2;
    private OrderTable orderTable1;
    private OrderLineItem orderLineItem1;
    private OrderLineItem orderLineItem2;
    private Order order1;

    public static Stream<List<OrderLineItem>> invalidOrderLineItemsParameter() {
        return Stream.of(null, Collections.emptyList());
    }

    @BeforeEach
    void setUp() {
        Long menuId1 = 1L;
        Product product1 = new Product(1L, "음식1", BigDecimal.ONE);
        Product product2 = new Product(2L, "음식2", BigDecimal.ONE);
        MenuGroup menuGroup1 = new MenuGroup(1L, "메뉴그룹1");
        MenuProduct menuProduct1 = new MenuProduct(1L, menuId1, product1.getId(), 1);
        MenuProduct menuProduct2 = new MenuProduct(2L, menuId1, product2.getId(), 1);
        menu1 = new Menu(menuId1, "메뉴1", BigDecimal.valueOf(2L), menuGroup1.getId(), Arrays.asList(menuProduct1, menuProduct2));

        Long menuId2 = 2L;
        Product product3 = new Product(3L, "음식3", BigDecimal.ONE);
        Product product4 = new Product(4L, "음식4", BigDecimal.ONE);
        MenuGroup menuGroup2 = new MenuGroup(2L, "메뉴그룹2");
        MenuProduct menuProduct3 = new MenuProduct(3L, menuId2, product3.getId(), 1);
        MenuProduct menuProduct4 = new MenuProduct(4L, menuId2, product4.getId(), 1);
        menu2 = new Menu(menuId2, "메뉴2", BigDecimal.valueOf(2L), menuGroup2.getId(), Arrays.asList(menuProduct3, menuProduct4));

        Long orderId1 = 1L;
        orderTable1 = new OrderTable(1L, null, 1, false);
        orderLineItem1 = new OrderLineItem(1L, orderId1, menu1.getId(), 1);
        orderLineItem2 = new OrderLineItem(2L, orderId1, menu2.getId(), 1);
        order1 = new Order(1L, orderTable1.getId(), OrderStatus.COOKING.name(), LocalDateTime.now(), Arrays.asList(orderLineItem1, orderLineItem2));
    }

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() {
        // given
        OrderLineItem orderLineItem1 = new OrderLineItem(this.orderLineItem1.getMenuId(), this.orderLineItem1.getQuantity());
        OrderLineItem orderLineItem2 = new OrderLineItem(this.orderLineItem2.getMenuId(), this.orderLineItem2.getQuantity());
        Order order = new Order(orderTable1.getId(), OrderStatus.COOKING.name(), LocalDateTime.now(), Arrays.asList(orderLineItem1, orderLineItem2));

        given(menuDao.countByIdIn(Arrays.asList(menu1.getId(), menu2.getId()))).willReturn(Long.valueOf(order.getOrderLineItems().size()));
        given(orderTableDao.findById(order.getOrderTableId())).willReturn(Optional.of(orderTable1));
        given(orderDao.save(order)).willReturn(order1);
        given(orderLineItemDao.save(orderLineItem1)).willReturn(this.orderLineItem1);
        given(orderLineItemDao.save(orderLineItem2)).willReturn(this.orderLineItem2);

        // when
        Order savedOrder = orderService.create(order);

        // then
        assertThat(savedOrder).isEqualTo(order1);
    }

    @DisplayName("주문 항목을 1개 이상 입력해야 한다.")
    @ParameterizedTest
    @MethodSource("invalidOrderLineItemsParameter")
    void invalidOrderLineItems(List<OrderLineItem> orderLineItems) {
        // given
        Order order = new Order(orderTable1.getId(), OrderStatus.COOKING.name(), LocalDateTime.now(), orderLineItems);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 항목들은 등록된 메뉴만 가능하다.")
    @Test
    void invalidMenuIdOfOrderLineItem() {
        // given
        Long menuId = 0L;
        Order order = new Order(orderTable1.getId(), OrderStatus.COOKING.name(), LocalDateTime.now(),
                                Collections.singletonList(new OrderLineItem(0L, 0L, menuId, 0)));

        given(menuDao.countByIdIn(Collections.singletonList(menuId))).willReturn(0L);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("등록된 주문 테이블만 주문을 할 수 있다.")
    @Test
    void notExistsOrderTable() {
        // given
        List<OrderLineItem> orderLineItems = Arrays.asList(orderLineItem1, orderLineItem2);
        Order order = new Order(0L, OrderStatus.COOKING.name(), LocalDateTime.now(), orderLineItems);

        given(menuDao.countByIdIn(Arrays.asList(orderLineItem1.getMenuId(), orderLineItem2.getMenuId())))
                .willReturn(Long.valueOf(orderLineItems.size()));
        given(orderTableDao.findById(order.getOrderTableId())).willReturn(Optional.empty());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("빈 테이블은 주문을 할 수 없다.")
    @Test
    void cannotEmptyOrderTable() {
        // given
        OrderTable orderTable1 = new OrderTable(1L, null, 1, true);
        List<OrderLineItem> orderLineItems = Arrays.asList(orderLineItem1, orderLineItem2);
        Order order = new Order(orderTable1.getId(), OrderStatus.COOKING.name(), LocalDateTime.now(), orderLineItems);

        given(menuDao.countByIdIn(Arrays.asList(orderLineItem1.getMenuId(), orderLineItem2.getMenuId())))
                .willReturn(Long.valueOf(orderLineItems.size()));
        given(orderTableDao.findById(order.getOrderTableId())).willReturn(Optional.of(orderTable1));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> orderService.create(order);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문과 주문 항목의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        Long orderId2 = 2L;
        List<OrderLineItem> orderLineItems2 = Arrays.asList(new OrderLineItem(3L, orderId2, 3L, 1),
                                                           new OrderLineItem(4L, orderId2, 4L, 1));
        Order order2 = new Order(orderId2, 2L, OrderStatus.MEAL.name(), LocalDateTime.now(), Collections.emptyList());
        List<Order> orders = Arrays.asList(order1, order2);

        given(orderDao.findAll()).willReturn(orders);
        given(orderLineItemDao.findAllByOrderId(order1.getId())).willReturn(order1.getOrderLineItems());
        given(orderLineItemDao.findAllByOrderId(order2.getId())).willReturn(orderLineItems2);

        // when
        List<Order> findOrders = orderService.list();

        // then
        assertThat(findOrders).containsExactlyElementsOf(orders);
    }

    @DisplayName("주문의 주문 상태를 변경하고 주문과 주문 항목을 반환한다.")
    @Test
    void changeOrderStatus() {
        // given
        Order order = new Order(order1.getId(), order1.getOrderTableId(), OrderStatus.MEAL.name(), order1.getOrderedTime(), order1.getOrderLineItems());

        given(orderDao.findById(order1.getId())).willReturn(Optional.of(order1));
        given(orderLineItemDao.findAllByOrderId(order1.getId())).willReturn(order1.getOrderLineItems());

        // when
        Order savedOrder = orderService.changeOrderStatus(order1.getId(), order);

        // then
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.MEAL.name());
    }
}
