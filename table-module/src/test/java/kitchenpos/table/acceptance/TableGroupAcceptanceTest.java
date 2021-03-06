package kitchenpos.table.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.common.AcceptanceKafkaTest;
import kitchenpos.common.utils.RestUtils;
import kitchenpos.table.config.TestConfiguration;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("단체 지정 관련 기능")
public class TableGroupAcceptanceTest extends AcceptanceKafkaTest {
    private static final String URL = "/api/table-groups";
    private OrderTableResponse 빈_주문테이블1;
    private OrderTableResponse 빈_주문테이블2;

    @BeforeEach
    public void setUp() {
        super.setUp();

        // given
        빈_주문테이블1 = TableAcceptanceTest.주문_테이블_생성_요청(TableAcceptanceTest.빈_주문테이블1_요청).as(OrderTableResponse.class);
        빈_주문테이블2 = TableAcceptanceTest.주문_테이블_생성_요청(TableAcceptanceTest.빈_주문테이블2_요청).as(OrderTableResponse.class);

        주문_모듈에서_완료_주문_상태로_메시지_수신받도록_설정();
    }

    @DisplayName("주문 테이블을 단체 지정을 등록하고 등록한 단체 지정을 반환한다.")
    @Test
    void create() {
        // when
        ExtractableResponse<Response> 단체_지정_등록_응답 = 단체_지정_등록_요청(빈_주문테이블1, 빈_주문테이블2);

        // then
        단체_지정_등록됨(단체_지정_등록_응답);
        단체_지정_포함됨(단체_지정_등록_응답, 빈_주문테이블1, 빈_주문테이블2);
    }

    @DisplayName("주문 테이블은 2개 이상만 단체로 지정할 수 있다.")
    @TestFactory
    Stream<DynamicTest> cannotCreateZeroOrOneTable() {
        return Stream.of(
                DynamicTest.dynamicTest("0개 테이블 단체 지정 등록 요청", () -> {
                    // when
                    ExtractableResponse<Response> 단체_지정_등록_응답 = 단체_지정_등록_요청();

                    // then
                    단체_지정_등록_실패됨(단체_지정_등록_응답);
                }),
                DynamicTest.dynamicTest("1개 테이블 단체 지정 등록 요청", () -> {
                    // when
                    ExtractableResponse<Response> 단체_지정_등록_응답 = 단체_지정_등록_요청(빈_주문테이블1);

                    // then
                    단체_지정_등록_실패됨(단체_지정_등록_응답);
                })
        );
    }

    @DisplayName("등록된 주문 테이블만 단체로 지정할 수 있다.")
    @Test
    void cannotCreateNotExistsTable() {
        // given
        OrderTableResponse 등록되지_않은_주문_테이블 = OrderTableResponse.from(OrderTable.of(-1L, null, 0, true));

        // when
        ExtractableResponse<Response> 단체_지정_등록_응답 = 단체_지정_등록_요청(빈_주문테이블1, 등록되지_않은_주문_테이블);

        // then
        단체_지정_등록_실패됨(단체_지정_등록_응답);
    }

    @DisplayName("주문 테이블이 비어있지 않거나, 이미 단체 지정이 되어 있으면 등록할 수 없다.")
    @TestFactory
    Stream<DynamicTest> cannotCreateNotEmptyOrExistsGroup() {
        return Stream.of(
                DynamicTest.dynamicTest("비어있지 않은 주문 테이블", () -> {
                    // given
                    OrderTableResponse 비어있지않은_주문테이블1 =
                            TableAcceptanceTest.주문_테이블_생성_요청(TableAcceptanceTest.비어있지않은_주문테이블1_요청)
                                    .as(OrderTableResponse.class);

                    // when
                    ExtractableResponse<Response> 단체_지정_등록_응답 = 단체_지정_등록_요청(빈_주문테이블1, 비어있지않은_주문테이블1);

                    // then
                    단체_지정_등록_실패됨(단체_지정_등록_응답);
                }),
                DynamicTest.dynamicTest("이미 단체 지정이 되어 있는 테이블", () -> {
                    // given
                    OrderTableResponse 빈_주문테이블3 =
                            TableAcceptanceTest.주문_테이블_생성_요청(new OrderTableRequest(0, true))
                                    .as(OrderTableResponse.class);
                    단체_지정_등록_요청(빈_주문테이블1, 빈_주문테이블2);

                    // when
                    ExtractableResponse<Response> 단체_지정_등록_응답 = 단체_지정_등록_요청(빈_주문테이블1, 빈_주문테이블3);

                    // then
                    단체_지정_등록_실패됨(단체_지정_등록_응답);
                })
        );
    }

    @DisplayName("단체 지정을 조회한다.")
    @Test
    void find() {
        // given
        TableGroupResponse 단체_지정 = 단체_지정_등록되어_있음(빈_주문테이블1, 빈_주문테이블2).as(TableGroupResponse.class);

        // when
        ExtractableResponse<Response> 단체_지정_조회_응답 = 단체_지정_조회_요청(단체_지정);

        // then
        단체_지정_응답됨(단체_지정_조회_응답);
        단체_지정_포함됨(단체_지정_조회_응답, 빈_주문테이블1, 빈_주문테이블2);
    }

    @DisplayName("단체 지정을 해제한다.")
    @Test
    void ungroup() {
        // given
        TableGroupResponse 단체_지정 = 단체_지정_등록되어_있음(빈_주문테이블1, 빈_주문테이블2).as(TableGroupResponse.class);

        // when
        ExtractableResponse<Response> 단체_지정_해제_응답 = 단체_지정_해제_요청(단체_지정);

        // then
        단체_지정_해제됨(단체_지정_해제_응답);
    }

    @DisplayName("주문 상태가 조리중이거나 식사인 경우에는 단체 지정을 해제할 수 없다.")
    @Test
    void cannotUngroupOrderStatusInCookingOrMeal() {
        // given
        TableGroupResponse 단체_지정 = 단체_지정_등록되어_있음(빈_주문테이블1, 빈_주문테이블2).as(TableGroupResponse.class);
        주문_모듈에서_조리또는식사_주문_상태로_메시지_수신받도록_설정();

        // when
        ExtractableResponse<Response> 단체_지정_해제_응답 = 단체_지정_해제_요청(단체_지정);

        // then
        단체_지정_해제됨(단체_지정_해제_응답);

        // 메시지 통신하는 동안 0.5초 대기 후 0.5초 간격으로 10초동안 확인
        await().pollDelay(Duration.ofMillis(500)).and().pollInterval(Duration.ofMillis(500)).atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            // when
            ExtractableResponse<Response> 단체_지정_조회_응답 = 단체_지정_조회_요청(단체_지정);

            // then
            단체_지정_응답됨(단체_지정_조회_응답);
            단체_지정_해제_취소되어있음(단체_지정_조회_응답, 빈_주문테이블1, 빈_주문테이블2);
        });
    }

    public static ExtractableResponse<Response> 단체_지정_등록_요청(OrderTableResponse... orderTableResponses) {
        List<OrderTableGroupRequest> orderTableGroupRequests = Stream.of(orderTableResponses)
                .map(orderTableResponse -> new OrderTableGroupRequest(orderTableResponse.getId()))
                .collect(Collectors.toList());
        TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTableGroupRequests);
        return RestUtils.post(URL, tableGroupRequest);
    }

    private void 단체_지정_등록됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    private void 단체_지정_포함됨(ExtractableResponse<Response> response, OrderTableResponse... orderTableResponses) {
        TableGroupResponse tableGroupResponse = response.as(TableGroupResponse.class);
        List<Long> tableGroupIds = tableGroupResponse.getOrderTables().stream()
                .map(OrderTableResponse::getId)
                .collect(Collectors.toList());
        List<Long> orderTableIds = Stream.of(orderTableResponses).map(OrderTableResponse::getId)
                .collect(Collectors.toList());
        assertThat(tableGroupResponse.getId()).isNotNull();
        assertThat(tableGroupResponse.getCreatedDate()).isNotNull();
        assertThat(tableGroupIds).containsAll(orderTableIds);
    }

    private void 단체_지정_등록_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private ExtractableResponse<Response> 단체_지정_등록되어_있음(OrderTableResponse... orderTableResponses) {
        return 단체_지정_등록_요청(orderTableResponses);
    }

    private ExtractableResponse<Response> 단체_지정_해제_요청(TableGroupResponse tableGroupResponse) {
        return RestUtils.delete(URL + String.format("/%d", tableGroupResponse.getId()));
    }

    private void 단체_지정_해제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    private ExtractableResponse<Response> 단체_지정_조회_요청(TableGroupResponse tableGroupResponse) {
        return RestUtils.get(URL + "/" + tableGroupResponse.getId());
    }

    private void 단체_지정_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 단체_지정_해제_취소되어있음(ExtractableResponse<Response> response, OrderTableResponse... orderTableResponses) {
        단체_지정_포함됨(response, orderTableResponses);
    }

    private void 주문_모듈에서_완료_주문_상태로_메시지_수신받도록_설정() {
        TestConfiguration.UNCOMPLETED_ORDER = Boolean.FALSE;
    }

    private void 주문_모듈에서_조리또는식사_주문_상태로_메시지_수신받도록_설정() {
        TestConfiguration.UNCOMPLETED_ORDER = Boolean.TRUE;
    }
}
