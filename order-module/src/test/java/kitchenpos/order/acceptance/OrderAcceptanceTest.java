package kitchenpos.order.acceptance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.common.AcceptanceTest;
import kitchenpos.common.utils.RestUtils;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.order.dto.OrderStatusChangeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("주문 관련 기능")
@EmbeddedKafka(brokerProperties = {"listeners=PLAINTEXT://${spring.kafka.bootstrap-servers}", "port=${spring.kafka.port}"})
public class OrderAcceptanceTest extends AcceptanceTest {
    private static final String URL = "/api/orders";
    private static OrderRequest 주문1_요청;
    private static OrderRequest 주문2_요청;
    private static Long 주문테이블1;
    private static Long 주문테이블2;
    private static Long 메뉴1;
    private static Long 메뉴2;
    private static Long 메뉴3;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        super.setUp();

        // given
        샘플_준비();

        메뉴_모듈에서_메뉴_있음_수신받도록_설정();
        테이블_모듈_메시지_스트림_수신_메시지_설정(주문_테이블_있음, 주문_테이블_비어있지않음);
    }

    @DisplayName("주문을 등록하고 등록한 주문과 주문 항목을 반환한다.")
    @Test
    void create() {
        // when
        ExtractableResponse<Response> 주문_생성_응답 = 주문_생성_요청(주문1_요청);

        // then
        주문_생성됨(주문_생성_응답, 주문1_요청);
    }

    @DisplayName("주문 항목을 1개 이상 입력해야 한다.")
    @Test
    void cannotCreateZeroOrderLineItem() {
        // given
        List<OrderLineItemRequest> 메뉴_0개_주문_항목 = Collections.emptyList();
        OrderRequest 메뉴_0개_주문_요청 = new OrderRequest(주문테이블1, 메뉴_0개_주문_항목);

        // when
        ExtractableResponse<Response> 주문_생성_응답 = 주문_생성_요청(메뉴_0개_주문_요청);

        // then
        주문_생성_실패됨(주문_생성_응답);
    }

    @DisplayName("주문 항목들은 등록된 메뉴만 가능하다.")
    @Test
    void cannotCreateNotExistsOrderLineItem() {
        // given
        Long 등록되지_않은_메뉴 = -1L;
        OrderRequest 등록되지_않은_메뉴로_주문_요청 = new OrderRequest(주문테이블1,
                                                          Collections.singletonList(
                                                                  new OrderLineItemRequest(등록되지_않은_메뉴, 1)));
        메뉴_모듈에서_메뉴_없음_수신받도록_설정();

        // when
        ExtractableResponse<Response> 주문_생성_응답 = 주문_생성_요청(등록되지_않은_메뉴로_주문_요청);

        // then
        주문_생성_실패됨(주문_생성_응답);
    }

    @DisplayName("등록된 주문 테이블만 주문을 할 수 있다.")
    @Test
    void cannotCreateNotExistsOrderTable() {
        // given
        Long 등록되지_않은_주문_테이블 = -1L;
        OrderRequest 등록되지_않은_주문_테이블에서_주문_요청 =
                new OrderRequest(등록되지_않은_주문_테이블, Collections.singletonList(new OrderLineItemRequest(메뉴1, 1)));
        테이블_모듈_메시지_스트림_수신_메시지_설정(주문_테이블_없음, 주문_테이블_비어있지않음);

        // when
        ExtractableResponse<Response> 주문_생성_응답 = 주문_생성_요청(등록되지_않은_주문_테이블에서_주문_요청);

        // then
        주문_생성_실패됨(주문_생성_응답);
    }

    @DisplayName("빈 테이블은 주문을 할 수 없다.")
    @Test
    void cannotCreateEmptyOrderTable() {
        // given
        Long 빈_주문테이블 = 1L;
        OrderRequest 빈_테이블에서_주문_요청 =
                new OrderRequest(빈_주문테이블, Collections.singletonList(new OrderLineItemRequest(메뉴1, 1)));
        테이블_모듈_메시지_스트림_수신_메시지_설정(주문_테이블_있음, 주문_테이블_비어있음);

        // when
        ExtractableResponse<Response> 주문_생성_응답 = 주문_생성_요청(빈_테이블에서_주문_요청);

        // then
        주문_생성_실패됨(주문_생성_응답);
    }

    @DisplayName("주문과 주문 항목의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        ExtractableResponse<Response> 주문1_응답 = 주문_등록되어_있음(주문1_요청);
        ExtractableResponse<Response> 주문2_응답 = 주문_등록되어_있음(주문2_요청);

        // when
        ExtractableResponse<Response> 주문_목록_조회_응답 = 주문_목록_조회_요청();

        // then
        주문_목록_응답됨(주문_목록_조회_응답);
        주문_목록_포함됨(주문_목록_조회_응답, Arrays.asList(주문1_응답, 주문2_응답));
    }

    @DisplayName("주문의 주문 상태를 변경하고 주문과 주문 항목을 반환한다.")
    @Test
    void changeOrderStatus() {
        // given
        OrderResponse 주문1 = 주문_등록되어_있음(주문1_요청).as(OrderResponse.class);
        OrderStatus 주문상태_식사 = OrderStatus.MEAL;

        // when
        ExtractableResponse<Response> 주문_상태_변경_응답 = 주문_상태_변경_요청(주문1, 주문상태_식사);

        // then
        주문_상태_변경됨(주문_상태_변경_응답, 주문상태_식사);
    }

    @DisplayName("주문 상태가 완료인 경우 주문 상태를 변경할 수 없다.")
    @Test
    void cannotChangeOrderStatusWasCompletion() {
        // given
        OrderResponse 주문1 = 주문_등록되어_있음(주문1_요청).as(OrderResponse.class);
        OrderStatus 주문상태_완료 = OrderStatus.COMPLETION;
        주문_상태_변경되어_있음(주문1, 주문상태_완료);
        OrderStatus 주문상태_식사 = OrderStatus.MEAL;

        // when
        ExtractableResponse<Response> 주문_상태_변경_응답 = 주문_상태_변경_요청(주문1, 주문상태_식사);

        // then
        주문_상태_변경_실패됨(주문_상태_변경_응답);
    }

    public static ExtractableResponse<Response> 주문_생성_요청(OrderRequest orderRequest) {
        return RestUtils.post(URL, orderRequest);
    }

    private void 주문_생성됨(ExtractableResponse<Response> response, OrderRequest orderRequest) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
        Long id = Long.parseLong(response.header("Location").split("/")[3]);

        OrderResponse orderResponse = response.as(OrderResponse.class);
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse.getOrderTableId()).isEqualTo(orderRequest.getOrderTableId());
        assertThat(orderResponse.getOrderStatus()).isEqualTo(OrderStatus.COOKING.name());
        assertThat(orderResponse.getOrderedTime()).isNotNull();
        assertThat(orderResponse.getOrderLineItems()).allMatch(
                orderLineItemResponse -> orderLineItemResponse.getSeq() != null);
        assertThat(orderResponse.getOrderLineItems()).extracting("orderId")
                .containsExactly(IntStream.range(0, 2).mapToLong((i) -> id).boxed().toArray());
        assertThat(orderResponse.getOrderLineItems()).extracting("menuId")
                .containsExactly(
                        orderRequest.getOrderLineItems().stream().map(OrderLineItemRequest::getMenuId).toArray());
        assertThat(orderResponse.getOrderLineItems()).extracting("quantity")
                .containsExactly(
                        orderRequest.getOrderLineItems().stream().map(OrderLineItemRequest::getQuantity).toArray());
    }

    private void 주문_생성_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private ExtractableResponse<Response> 주문_등록되어_있음(OrderRequest orderRequest) {
        return 주문_생성_요청(orderRequest);
    }

    private ExtractableResponse<Response> 주문_목록_조회_요청() {
        return RestUtils.get(URL);
    }

    private void 주문_목록_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 주문_목록_포함됨(ExtractableResponse<Response> response,
                           List<ExtractableResponse<Response>> createdResponses) {
        List<Long> expectedIds = createdResponses.stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[3]))
                .collect(Collectors.toList());

        List<Long> actualIds = response.jsonPath().getList(".", OrderResponse.class).stream()
                .map(OrderResponse::getId)
                .collect(Collectors.toList());

        assertThat(actualIds).containsAll(expectedIds);
    }

    public static ExtractableResponse<Response> 주문_상태_변경_요청(OrderResponse orderResponse, OrderStatus orderStatus) {
        return RestUtils.put(URL + String.format("/%d/order-status", orderResponse.getId()),
                             new OrderStatusChangeRequest(orderStatus.name()));
    }

    public static ExtractableResponse<Response> 주문_상태_변경되어_있음(OrderResponse orderResponse, OrderStatus orderStatus) {
        return 주문_상태_변경_요청(orderResponse, orderStatus);
    }

    private void 주문_상태_변경됨(ExtractableResponse<Response> response, OrderStatus orderStatus) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        OrderResponse orderResponse = response.as(OrderResponse.class);
        assertThat(orderResponse.getOrderStatus()).isEqualTo(orderStatus.name());
    }

    private void 주문_상태_변경_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private static void 샘플_준비() {
        샘플_메뉴_생성();
        샘플_주문_테이블_생성();

        주문1_요청 = new OrderRequest(주문테이블1,
                                  Arrays.asList(new OrderLineItemRequest(메뉴1, 1),
                                                new OrderLineItemRequest(메뉴2, 1)));
        주문2_요청 = new OrderRequest(주문테이블2,
                                  Arrays.asList(new OrderLineItemRequest(메뉴2, 1),
                                                new OrderLineItemRequest(메뉴3, 1)));
    }

    private static void 샘플_메뉴_생성() {
        메뉴1 = 1L;
        메뉴2 = 2L;
        메뉴3 = 3L;
    }

    private static void 샘플_주문_테이블_생성() {
        주문테이블1 = 1L;
        주문테이블2 = 2L;
    }

    /*private static List<OrderTableResponse> 샘플_주문_테이블_생성() {
        주문테이블1 = TableAcceptanceTest.주문_테이블_생성_요청(TableAcceptanceTest.빈_주문테이블1_요청).as(OrderTableResponse.class);
        주문테이블2 = TableAcceptanceTest.주문_테이블_생성_요청(TableAcceptanceTest.빈_주문테이블2_요청).as(OrderTableResponse.class);

        TableAcceptanceTest.주문_테이블_빈_테이블_여부_변경_요청(주문테이블1, TableAcceptanceTest.비어있지_않은_상태);
        TableAcceptanceTest.주문_테이블_손님_등록_요청(주문테이블1, TableAcceptanceTest.손님_4명);
        TableAcceptanceTest.주문_테이블_빈_테이블_여부_변경_요청(주문테이블2, TableAcceptanceTest.비어있지_않은_상태);
        TableAcceptanceTest.주문_테이블_손님_등록_요청(주문테이블2, TableAcceptanceTest.손님_4명);

        return Arrays.asList(주문테이블1, 주문테이블2);
    }

    public static List<MenuResponse> 샘플_메뉴_생성() {
        ProductResponse 음식1 = ProductAcceptanceTest.상품_생성_요청(ProductAcceptanceTest.음식1_요청).as(ProductResponse.class);
        ProductResponse 음식2 = ProductAcceptanceTest.상품_생성_요청(ProductAcceptanceTest.음식2_요청).as(ProductResponse.class);
        ProductResponse 음식3 = ProductAcceptanceTest.상품_생성_요청(ProductAcceptanceTest.음식3_요청).as(ProductResponse.class);

        MenuGroupResponse 메뉴그룹1 = MenuGroupAcceptanceTest.메뉴_그룹_생성_요청(MenuGroupAcceptanceTest.메뉴그룹1_요청)
                .as(MenuGroupResponse.class);

        메뉴1 = MenuAcceptanceTest.메뉴_생성_요청(
                        new MenuRequest("메뉴1(음식1+음식2)", BigDecimal.valueOf(13_000), 메뉴그룹1.getId(),
                                        Arrays.asList(
                                                new MenuProductRequest(음식1.getId(), 1),
                                                new MenuProductRequest(음식2.getId(), 1))))
                .as(MenuResponse.class);
        메뉴2 = MenuAcceptanceTest.메뉴_생성_요청(
                        new MenuRequest("메뉴2(음식2+음식3)", BigDecimal.valueOf(15_000), 메뉴그룹1.getId(),
                                        Arrays.asList(
                                                new MenuProductRequest(음식2.getId(), 1),
                                                new MenuProductRequest(음식3.getId(), 1))))
                .as(MenuResponse.class);
        메뉴3 = MenuAcceptanceTest.메뉴_생성_요청(
                        new MenuRequest("메뉴3(음식1+음식3)", BigDecimal.valueOf(14_000), 메뉴그룹1.getId(),
                                        Arrays.asList(
                                                new MenuProductRequest(음식1.getId(), 1),
                                                new MenuProductRequest(음식3.getId(), 1))))
                .as(MenuResponse.class);

        return Arrays.asList(메뉴1, 메뉴2, 메뉴3);
    }*/

    private static String 메뉴_모듈_수신_메시지;
    private static final String 메뉴_모듈_수신_메시지_메뉴_있음 = "{\"menuIds\":%s,\"exists\":true}";
    private static final String 메뉴_모듈_수신_메시지_메뉴_없음 = "{\"menuIds\":%s,\"exists\":false}";

    private void 메뉴_모듈에서_메뉴_있음_수신받도록_설정() {
        메뉴_모듈_수신_메시지 = 메뉴_모듈_수신_메시지_메뉴_있음;
    }

    private void 메뉴_모듈에서_메뉴_없음_수신받도록_설정() {
        메뉴_모듈_수신_메시지 = 메뉴_모듈_수신_메시지_메뉴_없음;
    }

    @KafkaListener(topics = "${kafka.topics.exists-menus}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String existsMenusListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        acknowledgment.acknowledge();
        return String.format(메뉴_모듈_수신_메시지,
                             objectMapper.readTree(payload)
                                     .get("menuIds")
                                     .toString());
    }

    private static String 테이블_모듈_수신_메시지;
    private static final boolean 주문_테이블_있음 = true;
    private static final boolean 주문_테이블_없음 = false;
    private static final boolean 주문_테이블_비어있음 = true;
    private static final boolean 주문_테이블_비어있지않음 = false;

    private void 테이블_모듈_메시지_스트림_수신_메시지_설정(boolean exists, boolean empty) {
        테이블_모듈_수신_메시지 = "{\"orderTableId\":%d,\"exists\":" + exists + ",\"empty\":" + empty + "}";
    }

    @KafkaListener(topics = "${kafka.topics.exists-and-empty-table}", containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    protected String existsAndEmptyTableListener(@Payload String payload, Acknowledgment acknowledgment)
            throws JsonProcessingException {
        acknowledgment.acknowledge();
        return String.format(테이블_모듈_수신_메시지,
                             objectMapper.readTree(payload)
                                     .get("orderTableId")
                                     .asLong());
    }
}
