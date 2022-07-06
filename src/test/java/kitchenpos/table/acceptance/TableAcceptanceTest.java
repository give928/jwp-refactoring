package kitchenpos.table.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.AcceptanceTest;
import kitchenpos.menu.acceptance.MenuAcceptanceTest;
import kitchenpos.menu.acceptance.MenuGroupAcceptanceTest;
import kitchenpos.menu.dto.MenuGroupResponse;
import kitchenpos.menu.dto.MenuProductRequest;
import kitchenpos.menu.dto.MenuRequest;
import kitchenpos.menu.dto.MenuResponse;
import kitchenpos.order.acceptance.OrderAcceptanceTest;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.product.acceptance.ProductAcceptanceTest;
import kitchenpos.product.dto.ProductResponse;
import kitchenpos.table.dto.*;
import kitchenpos.utils.RestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("주문 테이블 관련 기능")
public class TableAcceptanceTest extends AcceptanceTest {
    private static final String URL = "/api/tables";
    public static OrderTableRequest 빈_주문테이블1_요청 = new OrderTableRequest(0, true);
    public static OrderTableRequest 빈_주문테이블2_요청 = new OrderTableRequest(0, true);
    public static OrderTableRequest 비어있지않은_주문테이블1_요청 = new OrderTableRequest(0, false);
    public static boolean 비어있지_않은_상태 = Boolean.FALSE;
    public static boolean 비어있는_상태 = Boolean.TRUE;
    public static int 손님_4명 = 4;
    private int 손님_마이너스1명 = -1;

    @DisplayName("주문 테이블을 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void create() {
        // when
        ExtractableResponse<Response> 주문_테이블_생성_응답 = 주문_테이블_생성_요청(빈_주문테이블1_요청);

        // then
        주문_테이블_생성됨(주문_테이블_생성_응답, 빈_주문테이블1_요청);
    }

    @DisplayName("주문 테이블의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        ExtractableResponse<Response> 빈_주문테이블1_응답 = 주문_테이블_등록되어_있음(빈_주문테이블1_요청);
        ExtractableResponse<Response> 빈_주문테이블2_응답 = 주문_테이블_등록되어_있음(빈_주문테이블2_요청);
        ExtractableResponse<Response> 비어있지않은_주문테이블1_응답 = 주문_테이블_등록되어_있음(비어있지않은_주문테이블1_요청);

        // when
        ExtractableResponse<Response> 주문_테이블_목록_조회_응답 = 주문_테이블_목록_조회_요청();

        // then
        주문_테이블_목록_응답됨(주문_테이블_목록_조회_응답);
        주문_테이블_목록_포함됨(주문_테이블_목록_조회_응답,
                      Arrays.asList(빈_주문테이블1_응답, 빈_주문테이블2_응답, 비어있지않은_주문테이블1_응답));
    }

    @DisplayName("주문 테이블의 빈 테이블 여부를 변경하고 변경한 주문 테이블을 반환한다.")
    @Test
    void changeEmpty() {
        // given
        OrderTableResponse 빈_주문테이블 = 주문_테이블_등록되어_있음(빈_주문테이블1_요청).as(OrderTableResponse.class);

        // when
        ExtractableResponse<Response> 주문_테이블_빈_테이블_여부_변경_응답 = 주문_테이블_빈_테이블_여부_변경_요청(빈_주문테이블, 비어있지_않은_상태);

        // then
        주문_테이블_빈_테이블_여부_변경됨(주문_테이블_빈_테이블_여부_변경_응답, 비어있지_않은_상태);
    }

    @DisplayName("단체 지정된 주문 테이블은 빈 테이블 여부를 변경할 수 없다.")
    @Test
    void cannotChangeEmptyTableGroup() {
        // given
        OrderTableResponse 주문테이블1 = 주문_테이블_등록되어_있음(빈_주문테이블1_요청).as(OrderTableResponse.class);
        OrderTableResponse 주문테이블2 = 주문_테이블_등록되어_있음(빈_주문테이블2_요청).as(OrderTableResponse.class);
        TableGroupAcceptanceTest.단체_지정_등록_요청(주문테이블1, 주문테이블2);

        // when
        ExtractableResponse<Response> 주문_테이블_빈_테이블_여부_변경_응답 = 주문_테이블_빈_테이블_여부_변경_요청(주문테이블1, 비어있지_않은_상태);

        // then
        주문_테이블_빈_테이블_여부_변경_실패됨(주문_테이블_빈_테이블_여부_변경_응답);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 빈 테이블 여부를 변경할 수 없다.")
    @TestFactory
    Stream<DynamicTest> cannotUngroupOrderStatusInCookingOrMeal() {
        // given
        OrderTableResponse 주문테이블 = 주문_테이블_등록되어_있음(비어있지않은_주문테이블1_요청).as(OrderTableResponse.class);
        OrderResponse 주문 = 주문_생성(주문테이블);

        return Stream.of(
                DynamicTest.dynamicTest("주문 상태가 조리중인 경우 빈 테이블 여부를 변경", () -> {
                    // when
                    ExtractableResponse<Response> 주문_테이블_빈_테이블_여부_변경_응답 = 주문_테이블_빈_테이블_여부_변경_요청(주문테이블, 비어있는_상태);

                    // then
                    주문_테이블_빈_테이블_여부_변경_실패됨(주문_테이블_빈_테이블_여부_변경_응답);
                }),
                DynamicTest.dynamicTest("주문 상태가 식사인 경우 빈 테이블 여부를 변경", () -> {
                    // given
                    OrderStatus 주문상태_식사 = OrderStatus.MEAL;
                    OrderAcceptanceTest.주문_상태_변경_요청(주문, 주문상태_식사);

                    // when
                    ExtractableResponse<Response> 주문_테이블_빈_테이블_여부_변경_응답 = 주문_테이블_빈_테이블_여부_변경_요청(주문테이블, 비어있는_상태);

                    // then
                    주문_테이블_빈_테이블_여부_변경_실패됨(주문_테이블_빈_테이블_여부_변경_응답);
                })
        );
    }

    @DisplayName("주문 테이블에 방문한 손님 수를 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void changeNumberOfGuests() {
        // given
        OrderTableResponse 비어있지않은_주문테이블 = 주문_테이블_등록되어_있음(비어있지않은_주문테이블1_요청).as(OrderTableResponse.class);

        // when
        ExtractableResponse<Response> 주문_테이블_손님_등록_응답 = 주문_테이블_손님_등록_요청(비어있지않은_주문테이블, 손님_4명);

        // then
        주문_테이블_손님_등록됨(주문_테이블_손님_등록_응답, 손님_4명);
    }

    @DisplayName("방문한 손님 수는 0명 이상만 가능하다.")
    @Test
    void cannotChangeNullOrNegativeNumberOfGuests() {
        // given
        OrderTableResponse 비어있지않은_주문테이블 = 주문_테이블_등록되어_있음(비어있지않은_주문테이블1_요청).as(OrderTableResponse.class);

        // when
        ExtractableResponse<Response> 주문_테이블_손님_등록_응답 = 주문_테이블_손님_등록_요청(비어있지않은_주문테이블, 손님_마이너스1명);

        // then
        주문_테이블_손님_등록_실패됨(주문_테이블_손님_등록_응답);
    }

    @DisplayName("빈 테이블은 방문한 손님 수를 등록할 수 없다.")
    @Test
    void cannotChangeEmptyTableNumberOfGuests() {
        // given
        OrderTableResponse 빈_주문테이블 = 주문_테이블_등록되어_있음(빈_주문테이블1_요청).as(OrderTableResponse.class);

        // when
        ExtractableResponse<Response> 주문_테이블_손님_등록_응답 = 주문_테이블_손님_등록_요청(빈_주문테이블, 손님_4명);

        // then
        주문_테이블_손님_등록_실패됨(주문_테이블_손님_등록_응답);
    }

    public static ExtractableResponse<Response> 주문_테이블_생성_요청(OrderTableRequest orderTableRequest) {
        return RestUtils.post(URL, orderTableRequest);
    }

    private void 주문_테이블_생성됨(ExtractableResponse<Response> response, OrderTableRequest orderTableRequest) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();

        OrderTableResponse orderTableResponse = response.as(OrderTableResponse.class);
        assertThat(orderTableResponse.getId()).isNotNull();
        assertThat(orderTableResponse.getTableGroupId()).isNull();
        assertThat(orderTableResponse.getNumberOfGuests()).isEqualTo(orderTableRequest.getNumberOfGuests());
        assertThat(orderTableResponse.isEmpty()).isEqualTo(orderTableRequest.isEmpty());
    }

    private ExtractableResponse<Response> 주문_테이블_등록되어_있음(OrderTableRequest orderTableRequest) {
        return 주문_테이블_생성_요청(orderTableRequest);
    }

    private ExtractableResponse<Response> 주문_테이블_목록_조회_요청() {
        return RestUtils.get(URL);
    }

    private void 주문_테이블_목록_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 주문_테이블_목록_포함됨(ExtractableResponse<Response> response, List<ExtractableResponse<Response>> createdResponses) {
        List<Long> expectedIds = createdResponses.stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[3]))
                .collect(Collectors.toList());

        List<Long> actualIds = response.jsonPath().getList(".", OrderTableResponse.class).stream()
                .map(OrderTableResponse::getId)
                .collect(Collectors.toList());

        assertThat(actualIds).containsAll(expectedIds);
    }

    public static ExtractableResponse<Response> 주문_테이블_빈_테이블_여부_변경_요청(OrderTableResponse orderTableResponse,
                                                                      boolean empty) {
        return RestUtils.put(URL + String.format("/%d/empty", orderTableResponse.getId()), new OrderTableChangeEmptyRequest(empty));
    }

    private void 주문_테이블_빈_테이블_여부_변경됨(ExtractableResponse<Response> response, boolean empty) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getBoolean("empty")).isEqualTo(empty);
    }

    private void 주문_테이블_빈_테이블_여부_변경_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    public static ExtractableResponse<Response> 주문_테이블_손님_등록_요청(OrderTableResponse orderTableResponse,
                                                                int numberOfGuests) {
        return RestUtils.put(URL + String.format("/%d/number-of-guests", orderTableResponse.getId()), new OrderTableChangeNumberOfGuestRequest(numberOfGuests));
    }

    private void 주문_테이블_손님_등록됨(ExtractableResponse<Response> response, int numberOfGuests) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getInt("numberOfGuests")).isEqualTo(numberOfGuests);
    }

    private void 주문_테이블_손님_등록_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    public static OrderResponse 주문_생성(OrderTableResponse orderTableResponse) {
        MenuResponse 메뉴 = 샘플_메뉴_생성();
        return OrderAcceptanceTest.주문_생성_요청(
                        new OrderRequest(orderTableResponse.getId(),
                                         Collections.singletonList(new OrderLineItemRequest(메뉴.getId(), 1))))
                .as(OrderResponse.class);
    }

    public static MenuResponse 샘플_메뉴_생성() {
        ProductResponse 음식1 = ProductAcceptanceTest.상품_생성_요청(ProductAcceptanceTest.음식1_요청).as(ProductResponse.class);

        MenuGroupResponse 메뉴그룹1 = MenuGroupAcceptanceTest.메뉴_그룹_생성_요청(MenuGroupAcceptanceTest.메뉴그룹1_요청)
                .as(MenuGroupResponse.class);

        return MenuAcceptanceTest.메뉴_생성_요청(
                        new MenuRequest("메뉴1(음식1+음식2)", BigDecimal.valueOf(6_000), 메뉴그룹1.getId(),
                                        Collections.singletonList(new MenuProductRequest(음식1.getId(), 1))))
                .as(MenuResponse.class);
    }
}
