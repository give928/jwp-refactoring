package kitchenpos.menu.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.AcceptanceTest;
import kitchenpos.menu.dto.MenuGroupResponse;
import kitchenpos.menu.dto.MenuProductRequest;
import kitchenpos.menu.dto.MenuRequest;
import kitchenpos.menu.dto.MenuResponse;
import kitchenpos.product.acceptance.ProductAcceptanceTest;
import kitchenpos.product.dto.ProductResponse;
import kitchenpos.utils.RestUtils;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("메뉴 관련 기능")
public class MenuAcceptanceTest extends AcceptanceTest {
    private static final String URL = "/api/menus";
    private static ProductResponse 음식1;
    private static ProductResponse 음식2;
    private static ProductResponse 음식3;
    private static ProductResponse 음식4;
    private static MenuGroupResponse 메뉴그룹1;
    public static MenuRequest 메뉴1_요청;
    private static MenuRequest 메뉴2_요청;

    @BeforeEach
    public void setUp() {
        super.setUp();

        // given
        샘플_준비();
    }

    @DisplayName("메뉴를 등록하고 등록한 메뉴을 반환한다.")
    @Test
    void create() {
        // when
        ExtractableResponse<Response> 메뉴_생성_응답 = 메뉴_생성_요청(메뉴1_요청);

        // then
        메뉴_생성됨(메뉴_생성_응답, 메뉴1_요청);
    }

    @DisplayName("메뉴의 가격은 필수로 입력해야 하고, 0원 이상만 가능하다.")
    @TestFactory
    Stream<DynamicTest> createNullOrNegativePrice() {
        return Stream.of(
                DynamicTest.dynamicTest("가격이 없는 메뉴", () -> {
                    // given
                    MenuRequest 가격이_없는_메뉴 = new MenuRequest("메뉴1", null, 메뉴그룹1.getId(),
                                                            Arrays.asList(new MenuProductRequest(음식1.getId(), 1),
                                                                          new MenuProductRequest(음식2.getId(), 1)));

                    // when
                    ExtractableResponse<Response> 메뉴_생성_응답 = 메뉴_생성_요청(가격이_없는_메뉴);

                    // then
                    메뉴_생성_실패됨(메뉴_생성_응답);
                }),
                DynamicTest.dynamicTest("가격이 0원미만인 메뉴", () -> {
                    // given
                    MenuRequest 가격이_0원미만인_메뉴 = new MenuRequest("메뉴1", BigDecimal.valueOf(-1L), 메뉴그룹1.getId(),
                                                               Arrays.asList(new MenuProductRequest(음식1.getId(), 1),
                                                                             new MenuProductRequest(음식2.getId(), 1)));

                    // when
                    ExtractableResponse<Response> 메뉴_생성_응답 = 메뉴_생성_요청(가격이_0원미만인_메뉴);

                    // then
                    메뉴_생성_실패됨(메뉴_생성_응답);
                })
        );
    }

    @DisplayName("메뉴 그룹은 필수이고 등록된 메뉴 그룹만 가능하다.")
    @TestFactory
    Stream<DynamicTest> createNullOrNotExistsMenuGroup() {
        return Stream.of(
                DynamicTest.dynamicTest("메뉴 그룹이 없는 메뉴", () -> {
                    // given
                    MenuRequest 메뉴_그룹이_없는_메뉴 =
                            new MenuRequest("메뉴1", BigDecimal.ONE, null,
                                            Arrays.asList(new MenuProductRequest(음식1.getId(), 1),
                                                          new MenuProductRequest(음식2.getId(), 1)));

                    // when
                    ExtractableResponse<Response> 메뉴_생성_응답 = 메뉴_생성_요청(메뉴_그룹이_없는_메뉴);

                    // then
                    메뉴_생성_실패됨(메뉴_생성_응답);
                }),
                DynamicTest.dynamicTest("등록되지 않은 메뉴 그룹에 속하는 메뉴", () -> {
                    // given
                    MenuRequest 등록되지_않은_메뉴_그룹에_속하는_메뉴 =
                            new MenuRequest("메뉴1", BigDecimal.ONE, -1L,
                                            Arrays.asList(
                                                    new MenuProductRequest(음식1.getId(), 1),
                                                    new MenuProductRequest(음식2.getId(),
                                                                           1)));

                    // when
                    ExtractableResponse<Response> 메뉴_생성_응답 = 메뉴_생성_요청(등록되지_않은_메뉴_그룹에_속하는_메뉴);

                    // then
                    메뉴_생성_실패됨(메뉴_생성_응답);
                })
        );
    }

    @DisplayName("메뉴의 가격은 메뉴 상품들의 금액의 합 이하만 가능하다.")
    @Test
    void createInvalidMenuPrice() {
        // given
        MenuRequest 메뉴_상품들의_금액의_합을_초과하는_메뉴 =
                new MenuRequest("메뉴1", BigDecimal.valueOf(999_999L), 메뉴그룹1.getId(),
                                Arrays.asList(new MenuProductRequest(음식1.getId(), 1),
                                              new MenuProductRequest(음식2.getId(), 1)));

        // when
        ExtractableResponse<Response> 메뉴_생성_응답 = 메뉴_생성_요청(메뉴_상품들의_금액의_합을_초과하는_메뉴);

        // then
        메뉴_생성_실패됨(메뉴_생성_응답);
    }

    @DisplayName("메뉴의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        ExtractableResponse<Response> 메뉴1_응답 = 메뉴_등록되어_있음(메뉴1_요청);
        ExtractableResponse<Response> 메뉴2_응답 = 메뉴_등록되어_있음(메뉴2_요청);

        // when
        ExtractableResponse<Response> 메뉴_목록_조회_응답 = 메뉴_목록_조회_요청();

        // then
        메뉴_목록_응답됨(메뉴_목록_조회_응답);
        메뉴_목록_포함됨(메뉴_목록_조회_응답, Arrays.asList(메뉴1_응답, 메뉴2_응답));
    }

    public static ExtractableResponse<Response> 메뉴_생성_요청(MenuRequest menuRequest) {
        return RestUtils.post(URL, menuRequest);
    }

    private void 메뉴_생성됨(ExtractableResponse<Response> response, MenuRequest menuRequest) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
        Long id = Long.parseLong(response.header("Location").split("/")[3]);

        MenuResponse menuResponse = response.as(MenuResponse.class);
        assertThat(menuResponse.getId()).isNotNull();
        assertThat(menuResponse.getName()).isEqualTo(menuRequest.getName());
        assertThat(menuResponse.getPrice()).isEqualTo(menuRequest.getPrice());
        assertThat(menuResponse.getMenuGroupId()).isEqualTo(menuRequest.getMenuGroupId());
        assertThat(menuResponse.getMenuProducts()).allMatch(menuProductResponse -> menuProductResponse.getSeq() != null);
        assertThat(menuResponse.getMenuProducts()).extracting("menuId")
                .containsExactly(IntStream.range(0, 2).mapToLong((i) -> id).boxed().toArray());
        assertThat(menuResponse.getMenuProducts()).extracting("productId")
                .containsExactly(menuRequest.getMenuProducts().stream().map(MenuProductRequest::getProductId).toArray());
        assertThat(menuResponse.getMenuProducts()).extracting("quantity")
                .containsExactly(menuRequest.getMenuProducts().stream().map(MenuProductRequest::getQuantity).toArray());
    }

    private void 메뉴_생성_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private ExtractableResponse<Response> 메뉴_등록되어_있음(MenuRequest menuRequest) {
        return 메뉴_생성_요청(menuRequest);
    }

    private ExtractableResponse<Response> 메뉴_목록_조회_요청() {
        return RestUtils.get(URL);
    }

    private void 메뉴_목록_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 메뉴_목록_포함됨(ExtractableResponse<Response> response, List<ExtractableResponse<Response>> createdResponses) {
        List<Long> expectedIds = createdResponses.stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[3]))
                .collect(Collectors.toList());

        List<Long> actualIds = response.jsonPath().getList(".", MenuResponse.class).stream()
                .map(MenuResponse::getId)
                .collect(Collectors.toList());

        assertThat(actualIds).containsAll(expectedIds);
    }

    public static void 샘플_준비() {
        샘플_음식_생성();
        샘플_메뉴_그룹_생성();

        메뉴1_요청 = new MenuRequest("메뉴1", BigDecimal.valueOf(13_000), 메뉴그룹1.getId(),
                                 Arrays.asList(new MenuProductRequest(음식1.getId(), 1),
                                               new MenuProductRequest(음식2.getId(), 1)));
        메뉴2_요청 = new MenuRequest("메뉴2", BigDecimal.valueOf(17_000), 메뉴그룹1.getId(),
                                 Arrays.asList(new MenuProductRequest(음식3.getId(), 1),
                                               new MenuProductRequest(음식4.getId(), 1)));
    }

    private static void 샘플_음식_생성() {
        음식1 = ProductAcceptanceTest.상품_생성_요청(ProductAcceptanceTest.음식1_요청).as(ProductResponse.class);
        음식2 = ProductAcceptanceTest.상품_생성_요청(ProductAcceptanceTest.음식2_요청).as(ProductResponse.class);
        음식3 = ProductAcceptanceTest.상품_생성_요청(ProductAcceptanceTest.음식3_요청).as(ProductResponse.class);
        음식4 = ProductAcceptanceTest.상품_생성_요청(ProductAcceptanceTest.음식4_요청).as(ProductResponse.class);
    }

    private static void 샘플_메뉴_그룹_생성() {
        메뉴그룹1 = MenuGroupAcceptanceTest.메뉴_그룹_생성_요청(MenuGroupAcceptanceTest.메뉴그룹1_요청)
                .as(MenuGroupResponse.class);
    }
}
