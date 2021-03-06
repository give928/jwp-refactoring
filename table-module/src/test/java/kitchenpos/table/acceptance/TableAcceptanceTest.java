package kitchenpos.table.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.common.AcceptanceKafkaTest;
import kitchenpos.common.utils.RestUtils;
import kitchenpos.table.config.TestConfiguration;
import kitchenpos.table.dto.OrderTableChangeEmptyRequest;
import kitchenpos.table.dto.OrderTableChangeNumberOfGuestRequest;
import kitchenpos.table.dto.OrderTableRequest;
import kitchenpos.table.dto.OrderTableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("주문 테이블 관련 기능")
public class TableAcceptanceTest extends AcceptanceKafkaTest {
    private static final String URL = "/api/tables";
    public static OrderTableRequest 빈_주문테이블1_요청 = new OrderTableRequest(0, true);
    public static OrderTableRequest 빈_주문테이블2_요청 = new OrderTableRequest(0, true);
    public static OrderTableRequest 비어있지않은_주문테이블1_요청 = new OrderTableRequest(0, false);
    public static boolean 비어있지_않은_상태 = Boolean.FALSE;
    public static boolean 비어있는_상태 = Boolean.TRUE;
    public static int 손님_4명 = 4;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        // given
        주문_모듈에서_완료_주문_상태로_메시지_수신받도록_설정();
    }

    @DisplayName("주문 테이블을 등록하고 등록한 주문 테이블을 반환한다.")
    @Test
    void create() {
        // when
        ExtractableResponse<Response> 주문_테이블_생성_응답 = 주문_테이블_생성_요청(빈_주문테이블1_요청);

        // then
        주문_테이블_생성됨(주문_테이블_생성_응답, 빈_주문테이블1_요청);
    }

    @DisplayName("주문 테이블을 조회한다.")
    @Test
    void find() {
        // given
        OrderTableResponse 주문테이블 = 주문_테이블_등록되어_있음(빈_주문테이블1_요청).as(OrderTableResponse.class);

        // when
        ExtractableResponse<Response> 주문_테이블_조회_응답 = 주문_테이블_조회_요청(주문테이블);

        // then
        주문_테이블_응답됨(주문_테이블_조회_응답, 주문테이블);
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
    @Test
    void cannotUngroupOrderStatusInCookingOrMeal() {
        // given
        OrderTableResponse 주문테이블 = 주문_테이블_등록되어_있음(비어있지않은_주문테이블1_요청).as(OrderTableResponse.class);
        주문_모듈에서_조리또는식사_주문_상태로_메시지_수신받도록_설정();

        // when
        ExtractableResponse<Response> 주문_테이블_빈_테이블_여부_변경_응답 = 주문_테이블_빈_테이블_여부_변경_요청(주문테이블, 비어있는_상태);

        // then
        주문_테이블_빈_테이블_여부_변경됨(주문_테이블_빈_테이블_여부_변경_응답, 비어있는_상태);

        // 메시지 통신하는 동안 0.5초 대기 후 0.5초 간격으로 10초동안 확인
        await().pollDelay(Duration.ofMillis(500)).and().pollInterval(Duration.ofMillis(500)).atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            // when
            ExtractableResponse<Response> 주문_테이블_조회_응답 = 주문_테이블_조회_요청(주문테이블);

            // then
            주문_테이블_응답됨(주문_테이블_조회_응답);
            주문_테이블_빈_테이블_여부_변경_취소되어있음(주문_테이블_조회_응답, 비어있지_않은_상태);
        });
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
        int 손님_마이너스1명 = -1;

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

    private void 주문_테이블_빈_테이블_여부_변경_취소되어있음(ExtractableResponse<Response> response, boolean empty) {
        OrderTableResponse orderTableResponse = response.as(OrderTableResponse.class);
        assertThat(orderTableResponse.isEmpty()).isEqualTo(empty);
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

    private ExtractableResponse<Response> 주문_테이블_조회_요청(OrderTableResponse orderTableResponse) {
        return RestUtils.get(URL + "/" + orderTableResponse.getId());
    }

    private void 주문_테이블_응답됨(ExtractableResponse<Response> response, OrderTableResponse createdOrderTableResponse) {
        주문_테이블_응답됨(response);

        OrderTableResponse orderTableResponse = response.as(OrderTableResponse.class);
        assertThat(orderTableResponse.getId()).isEqualTo(createdOrderTableResponse.getId());
        assertThat(orderTableResponse.getTableGroupId()).isEqualTo(createdOrderTableResponse.getTableGroupId());
        assertThat(orderTableResponse.getNumberOfGuests()).isEqualTo(createdOrderTableResponse.getNumberOfGuests());
        assertThat(orderTableResponse.isEmpty()).isEqualTo(createdOrderTableResponse.isEmpty());
    }

    private void 주문_테이블_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 주문_모듈에서_완료_주문_상태로_메시지_수신받도록_설정() {
        TestConfiguration.UNCOMPLETED_ORDER = Boolean.FALSE;
    }

    private void 주문_모듈에서_조리또는식사_주문_상태로_메시지_수신받도록_설정() {
        TestConfiguration.UNCOMPLETED_ORDER = Boolean.TRUE;
    }
}
