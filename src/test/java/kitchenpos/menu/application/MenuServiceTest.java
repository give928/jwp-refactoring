package kitchenpos.menu.application;

import kitchenpos.menu.domain.*;
import kitchenpos.menu.dto.MenuProductRequest;
import kitchenpos.menu.dto.MenuRequest;
import kitchenpos.menu.dto.MenuResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kitchenpos.Fixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private MenuGroupRepository menuGroupRepository;
    @Mock
    private MenuValidator menuValidator;

    @InjectMocks
    private MenuService menuService;

    private Menu menu1;
    private Menu menu2;

    public static Stream<BigDecimal> invalidPriceParameter() {
        return Stream.of(null, BigDecimal.valueOf(-1));
    }

    public static Stream<Long> invalidMenuGroupParameter() {
        return Stream.of(null, -1L);
    }

    @BeforeEach
    void setUp() {
        menu1 = aMenu1();
        menu2 = aMenu2();
    }

    @DisplayName("메뉴를 등록하고 등록한 메뉴와 메뉴 상품을 반환한다.")
    @Test
    void create() {
        // given
        MenuRequest menuRequest = createMenuRequestBy(menu1);

        given(menuGroupRepository.findById(menuRequest.getMenuGroupId())).willReturn(Optional.of(menu1.getMenuGroup()));
        given(menuRepository.save(any())).willReturn(menu1);

        // when
        MenuResponse menuResponse = menuService.create(menuRequest);

        // then
        assertThat(menuResponse.getId()).isEqualTo(menu1.getId());
        assertThat(menuResponse.getName()).isEqualTo(menu1.getName());
        assertThat(menuResponse.getPrice()).isEqualTo(menu1.getPrice());
        assertThat(menuResponse.getMenuGroupId()).isEqualTo(menu1.getMenuGroup().getId());
        assertThat(menuResponse.getMenuProducts()).hasSameSizeAs(menu1.getMenuProducts());
    }

    @DisplayName("메뉴의 가격은 필수로 입력해야 하고, 0원 이상만 가능하다.")
    @ParameterizedTest
    @MethodSource("invalidPriceParameter")
    void invalidPrice(BigDecimal price) {
        // given
        MenuRequest menuRequest = new MenuRequest(menu1.getName(), price, menu1.getMenuGroup().getId(),
                                                  menu1.getMenuProducts().stream()
                                                          .map(menuProduct -> new MenuProductRequest(
                                                                  menuProduct.getProductId(),
                                                                  menuProduct.getQuantity()))
                                                          .collect(Collectors.toList()));

        given(menuGroupRepository.findById(menuRequest.getMenuGroupId())).willReturn(Optional.of(menu1.getMenuGroup()));

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
        MenuRequest menuRequest = new MenuRequest(menu1.getName(), menu1.getPrice(), menuGroupId,
                                                  menu1.getMenuProducts().stream()
                                                          .map(menuProduct -> new MenuProductRequest(
                                                                  menuProduct.getProductId(),
                                                                  menuProduct.getQuantity()))
                                                          .collect(Collectors.toList()));

        given(menuGroupRepository.findById(menuRequest.getMenuGroupId())).willReturn(Optional.empty());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuService.create(menuRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴의 가격은 메뉴 상품들의 금액의 합 이하만 가능하다.")
    @Test
    void invalidPrice() {
        // given
        MenuRequest menuRequest = new MenuRequest(menu1.getName(), BigDecimal.valueOf(3L), menu1.getMenuGroup().getId(),
                                                  menu1.getMenuProducts().stream()
                                                          .map(menuProduct -> new MenuProductRequest(
                                                                  menuProduct.getProductId(),
                                                                  menuProduct.getQuantity()))
                                                          .collect(Collectors.toList()));

        given(menuGroupRepository.findById(menuRequest.getMenuGroupId())).willReturn(Optional.of(menu1.getMenuGroup()));
        willThrow(IllegalArgumentException.class).given(menuValidator).create(any());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> menuService.create(menuRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴와 메뉴 상품의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        given(menuRepository.findAll()).willReturn(Arrays.asList(menu1, menu2));

        // when
        List<MenuResponse> menuResponses = menuService.list();

        // then
        assertThat(menuResponses).extracting("id").containsExactly(menu1.getId(), menu2.getId());
        assertThat(menuResponses).extracting("name").containsExactly(menu1.getName(), menu2.getName());
        assertThat(menuResponses).extracting("price").containsExactly(menu1.getPrice(), menu2.getPrice());
    }

    private MenuRequest createMenuRequestBy(Menu menu) {
        return new MenuRequest(menu.getName(), menu.getPrice(), menu.getMenuGroup().getId(),
                               menu.getMenuProducts().stream()
                                       .map(menuProduct -> new MenuProductRequest(
                                               menuProduct.getProductId(),
                                               menuProduct.getQuantity()))
                                       .collect(Collectors.toList()));
    }
}
