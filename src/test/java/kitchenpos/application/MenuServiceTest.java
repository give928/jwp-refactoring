package kitchenpos.application;

import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.MenuProductDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {
    @Mock
    private MenuDao menuDao;
    @Mock
    private MenuGroupDao menuGroupDao;
    @Mock
    private MenuProductDao menuProductDao;
    @Mock
    private ProductDao productDao;

    @InjectMocks
    private MenuService menuService;

    private Product product1;
    private Product product2;
    private MenuGroup menuGroup1;
    private MenuProduct menuProduct1;
    private MenuProduct menuProduct2;
    private Menu menu1;

    public static Stream<BigDecimal> invalidPriceParameter() {
        return Stream.of(null, BigDecimal.valueOf(-1));
    }

    public static Stream<Long> invalidMenuGroupParameter() {
        return Stream.of(null, -1L);
    }

    @BeforeEach
    void setUp() {
        Long menuId1 = 1L;
        product1 = new Product(1L, "음식1", BigDecimal.ONE);
        product2 = new Product(2L, "음식2", BigDecimal.ONE);
        menuGroup1 = new MenuGroup(1L, "메뉴그룹1");
        menuProduct1 = new MenuProduct(1L, menuId1, product1.getId(), 1);
        menuProduct2 = new MenuProduct(2L, menuId1, product2.getId(), 1);
        menu1 = new Menu(1L, "메뉴1", BigDecimal.valueOf(2L), menuGroup1.getId(), Arrays.asList(menuProduct1, menuProduct2));
    }

    @DisplayName("메뉴를 등록하고 등록한 메뉴와 메뉴 상품을 반환한다.")
    @Test
    void create() {
        // given
        MenuProduct menuProduct1 = new MenuProduct(this.menuProduct1.getProductId(), this.menuProduct1.getQuantity());
        MenuProduct menuProduct2 = new MenuProduct(this.menuProduct2.getProductId(), this.menuProduct2.getQuantity());
        Menu menu = new Menu("메뉴1", BigDecimal.valueOf(2L), menuGroup1.getId(), Arrays.asList(menuProduct1, menuProduct2));

        given(menuGroupDao.existsById(menu.getMenuGroupId())).willReturn(Optional.ofNullable(menuGroup1).isPresent());
        given(productDao.findById(menuProduct1.getProductId())).willReturn(Optional.of(product1));
        given(productDao.findById(menuProduct2.getProductId())).willReturn(Optional.of(product2));
        given(menuDao.save(menu)).willReturn(menu1);
        given(menuProductDao.save(menuProduct1)).willReturn(this.menuProduct1);
        given(menuProductDao.save(menuProduct2)).willReturn(this.menuProduct2);

        // when
        Menu savedMenu = menuService.create(menu);

        // then
        assertThat(savedMenu).isEqualTo(menu1);
    }

    @DisplayName("메뉴의 가격은 필수로 입력해야 하고, 0원 이상만 가능하다.")
    @ParameterizedTest
    @MethodSource("invalidPriceParameter")
    void invalidPrice(BigDecimal price) {
        // given
        Menu menu = new Menu("메뉴1", price, 1L, Collections.emptyList());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuService.create(menu);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴 그룹은 필수이고 등록된 메뉴 그룹만 가능하다.")
    @ParameterizedTest
    @MethodSource("invalidMenuGroupParameter")
    void invalidMenuGroup(Long menuGroupId) {
        // given
        Menu menu = new Menu("메뉴1", BigDecimal.ONE, menuGroupId, Collections.emptyList());

        given(menuGroupDao.existsById(menuGroupId)).willReturn(false);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuService.create(menu);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴의 가격은 메뉴 상품들의 금액의 합 이하만 가능하다.")
    @Test
    void invalidAmount() {
        // given
        MenuProduct menuProduct1 = new MenuProduct(this.menuProduct1.getProductId(), this.menuProduct1.getQuantity());
        MenuProduct menuProduct2 = new MenuProduct(this.menuProduct2.getProductId(), this.menuProduct2.getQuantity());
        Menu menu = new Menu("메뉴1", BigDecimal.valueOf(3L), menuGroup1.getId(), Arrays.asList(menuProduct1, menuProduct2));

        given(menuGroupDao.existsById(menu.getMenuGroupId())).willReturn(Optional.ofNullable(menuGroup1).isPresent());
        given(productDao.findById(menuProduct1.getProductId())).willReturn(Optional.of(product1));
        given(productDao.findById(menuProduct2.getProductId())).willReturn(Optional.of(product2));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuService.create(menu);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴와 메뉴 상품의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        Product product3 = new Product(3L, "음식1", BigDecimal.ONE);
        Product product4 = new Product(4L, "음식2", BigDecimal.ONE);
        List<MenuProduct> menuProducts1 = Arrays.asList(new MenuProduct(product1.getId(), 1), new MenuProduct(product2.getId(), 1));
        Menu menu1 = new Menu("메뉴1", BigDecimal.valueOf(2L), menuGroup1.getId(), Collections.emptyList());
        List<MenuProduct> menuProducts2 = Arrays.asList(new MenuProduct(product3.getId(), 1), new MenuProduct(product4.getId(), 1));
        Menu menu2 = new Menu("메뉴1", BigDecimal.valueOf(2L), menuGroup1.getId(), Collections.emptyList());
        List<Menu> menus = Arrays.asList(menu1, menu2);

        given(menuDao.findAll()).willReturn(menus);
        given(menuProductDao.findAllByMenuId(menu1.getId())).willReturn(menuProducts1);
        given(menuProductDao.findAllByMenuId(menu2.getId())).willReturn(menuProducts2);

        // when
        List<Menu> findMenus = menuService.list();

        // then
        assertThat(findMenus).containsExactlyElementsOf(menus);
    }
}
