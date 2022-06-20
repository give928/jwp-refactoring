package kitchenpos.menu.application;

import kitchenpos.menu.dao.MenuDao;
import kitchenpos.menu.dao.MenuGroupDao;
import kitchenpos.menu.dao.MenuProductDao;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.menu.dto.MenuProductRequest;
import kitchenpos.menu.dto.MenuRequest;
import kitchenpos.menu.dto.MenuResponse;
import kitchenpos.product.dao.ProductDao;
import kitchenpos.product.domain.Product;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
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
    private MenuGroup menuGroup;
    private MenuProduct menuProduct1;
    private MenuProduct menuProduct2;
    private Menu menu1;
    private MenuProduct menuProduct3;
    private MenuProduct menuProduct4;
    private Menu menu2;

    public static Stream<BigDecimal> invalidPriceParameter() {
        return Stream.of(null, BigDecimal.valueOf(-1));
    }

    public static Stream<Long> invalidMenuGroupParameter() {
        return Stream.of(null, -1L);
    }

    @BeforeEach
    void setUp() {
        Long savedMenuId1 = 1L;
        product1 = new Product(1L, "음식1", BigDecimal.ONE);
        product2 = new Product(2L, "음식2", BigDecimal.ONE);
        menuGroup = new MenuGroup(1L, "메뉴그룹1");
        menuProduct1 = new MenuProduct(1L, savedMenuId1, product1.getId(), 1);
        menuProduct2 = new MenuProduct(2L, savedMenuId1, product2.getId(), 1);
        menu1 = new Menu(savedMenuId1, "메뉴1", BigDecimal.valueOf(2L), menuGroup.getId(),
                         Arrays.asList(menuProduct1, menuProduct2));

        Long savedMenuId2 = 2L;
        Product product3 = new Product(3L, "음식1", BigDecimal.ONE);
        Product product4 = new Product(4L, "음식2", BigDecimal.ONE);
        menuProduct3 = new MenuProduct(3L, savedMenuId2, product3.getId(), 1);
        menuProduct4 = new MenuProduct(4L, savedMenuId2, product4.getId(), 1);
        menu2 = new Menu(savedMenuId2, "메뉴2", BigDecimal.valueOf(2L), menuGroup.getId(), Arrays.asList(menuProduct3, menuProduct4));
    }

    @DisplayName("메뉴를 등록하고 등록한 메뉴와 메뉴 상품을 반환한다.")
    @Test
    void create() {
        // given
        MenuRequest menuRequest = new MenuRequest(menu1.getName(), menu1.getPrice(), menu1.getMenuGroupId(),
                                                  menu1.getMenuProducts().stream()
                                                          .map(menuProduct -> new MenuProductRequest(
                                                                  menuProduct.getProductId(), menuProduct.getQuantity()))
                                                          .collect(Collectors.toList()));

        given(menuGroupDao.existsById(menu1.getMenuGroupId())).willReturn(
                Optional.ofNullable(menuGroup).isPresent());
        given(productDao.findById(product1.getId())).willReturn(Optional.of(product1));
        given(productDao.findById(product2.getId())).willReturn(Optional.of(product2));
        given(menuDao.save(menuRequest.toMenu())).willReturn(menu1);
        given(menuProductDao.save(argThat(argument -> argument != null && Objects.equals(argument.getProductId(), menuProduct1.getProductId()))))
                .willReturn(menuProduct1);
        given(menuProductDao.save(argThat(argument -> argument != null && Objects.equals(argument.getProductId(), menuProduct2.getProductId()))))
                .willReturn(menuProduct2);

        // when
        MenuResponse menuResponse = menuService.create(menuRequest);

        // then
        assertThat(menuResponse.getId()).isEqualTo(menu1.getId());
        assertThat(menuResponse.getName()).isEqualTo(menu1.getName());
        assertThat(menuResponse.getPrice()).isEqualTo(menu1.getPrice());
        assertThat(menuResponse.getMenuGroupId()).isEqualTo(menu1.getMenuGroupId());
        assertThat(menuResponse.getMenuProducts()).hasSameSizeAs(menu1.getMenuProducts());
    }

    @DisplayName("메뉴의 가격은 필수로 입력해야 하고, 0원 이상만 가능하다.")
    @ParameterizedTest
    @MethodSource("invalidPriceParameter")
    void invalidPrice(BigDecimal price) {
        // given
        MenuRequest menuRequest = new MenuRequest(menu1.getName(), price, menu1.getMenuGroupId(), Collections.emptyList());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuService.create(menuRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴 그룹은 필수이고 등록된 메뉴 그룹만 가능하다.")
    @ParameterizedTest
    @MethodSource("invalidMenuGroupParameter")
    void invalidMenuGroup(Long menuGroupId) {
        // given
        MenuRequest menuRequest = new MenuRequest(menu1.getName(), menu1.getPrice(), menuGroupId, Collections.emptyList());

        given(menuGroupDao.existsById(menuGroupId)).willReturn(false);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuService.create(menuRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴의 가격은 메뉴 상품들의 금액의 합 이하만 가능하다.")
    @Test
    void invalidAmount() {
        // given
        MenuRequest menuRequest = new MenuRequest(menu1.getName(), BigDecimal.valueOf(3L), menu1.getMenuGroupId(),
                                                  menu1.getMenuProducts().stream()
                                                          .map(menuProduct -> new MenuProductRequest(
                                                                  menuProduct.getProductId(), menuProduct.getQuantity()))
                                                          .collect(Collectors.toList()));

        given(menuGroupDao.existsById(menu1.getMenuGroupId())).willReturn(Optional.ofNullable(menuGroup).isPresent());
        given(productDao.findById(product1.getId())).willReturn(Optional.of(product1));
        given(productDao.findById(product2.getId())).willReturn(Optional.of(product2));

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuService.create(menuRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴와 메뉴 상품의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        Menu menu1 = new Menu(1L, this.menu1.getName(), this.menu1.getPrice(), this.menu1.getMenuGroupId(), Collections.emptyList());
        Menu menu2 = new Menu(2L, this.menu2.getName(), this.menu2.getPrice(), this.menu2.getMenuGroupId(), Collections.emptyList());
        List<Menu> menus = Arrays.asList(menu1, menu2);

        given(menuDao.findAll()).willReturn(menus);
        given(menuProductDao.findAllByMenuId(menu1.getId())).willReturn(Arrays.asList(menuProduct1, menuProduct2));
        given(menuProductDao.findAllByMenuId(menu2.getId())).willReturn(Arrays.asList(menuProduct3, menuProduct4));

        // when
        List<MenuResponse> menuResponses = menuService.list();

        // then
        assertThat(menuResponses).extracting("id").containsExactly(menu1.getId(), menu2.getId());
        assertThat(menuResponses).extracting("name").containsExactly(menu1.getName(), menu2.getName());
        assertThat(menuResponses).extracting("price").containsExactly(menu1.getPrice(), menu2.getPrice());
    }
}
