package kitchenpos.product.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.AcceptanceTest;
import kitchenpos.common.domain.Name;
import kitchenpos.product.dto.ProductRequest;
import kitchenpos.product.dto.ProductResponse;
import kitchenpos.utils.RestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("상품 관련 기능")
public class ProductAcceptanceTest extends AcceptanceTest {
    private static final String URL = "/api/products";
    public static ProductRequest 음식1_요청 = new ProductRequest("음식1", BigDecimal.valueOf(6_000));
    public static ProductRequest 음식2_요청 = new ProductRequest("음식2", BigDecimal.valueOf(7_000));
    public static ProductRequest 음식3_요청 = new ProductRequest("음식3", BigDecimal.valueOf(8_000));
    public static ProductRequest 음식4_요청 = new ProductRequest("음식4", BigDecimal.valueOf(9_000));

    @DisplayName("상품을 등록하고 등록한 상품을 반환한다.")
    @Test
    void create() {
        // when
        ExtractableResponse<Response> 상품_생성_응답 = 상품_생성_요청(음식1_요청);

        // then
        상품_생성됨(상품_생성_응답, 음식1_요청);
    }

    @DisplayName("상품의 가격은 필수로 입력해야 하고, 0원 이상만 가능하다.")
    @TestFactory
    Stream<DynamicTest> createNullOrNegativePrice() {
        return Stream.of(
                DynamicTest.dynamicTest("가격이 없는 음식", () -> {
                    // given
                    ProductRequest 가격이_없는_음식 = new ProductRequest("가격이 없는 음식", null);

                    // when
                    ExtractableResponse<Response> 상품_생성_응답 = 상품_생성_요청(가격이_없는_음식);

                    // then
                    상품_생성_실패됨(상품_생성_응답);
                }),
                DynamicTest.dynamicTest("가격이 0원미만인 음식", () -> {
                    // given
                    ProductRequest 가격이_0원미만인_음식 = new ProductRequest("가격이 0원미만인 음식", BigDecimal.valueOf(-1L));

                    // when
                    ExtractableResponse<Response> 상품_생성_응답 = 상품_생성_요청(가격이_0원미만인_음식);

                    // then
                    상품_생성_실패됨(상품_생성_응답);
                })
        );
    }

    @DisplayName("상품의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        ExtractableResponse<Response> 음식1_응답 = 상품_등록되어_있음(음식1_요청);
        ExtractableResponse<Response> 음식2_응답 = 상품_등록되어_있음(음식2_요청);

        // when
        ExtractableResponse<Response> 상품_목록_조회_응답 = 상품_목록_조회_요청();

        // then
        상품_목록_응답됨(상품_목록_조회_응답);
        상품_목록_포함됨(상품_목록_조회_응답, Arrays.asList(음식1_응답, 음식2_응답));
    }

    public static ExtractableResponse<Response> 상품_생성_요청(ProductRequest productRequest) {
        return RestUtils.post(URL, productRequest);
    }

    private void 상품_생성됨(ExtractableResponse<Response> response, ProductRequest productRequest) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();

        ProductResponse productResponse = response.as(ProductResponse.class);
        assertThat(productResponse.getId()).isNotNull();
        assertThat(productResponse.getName()).isEqualTo(productRequest.getName());
        assertThat(productResponse.getPrice()).isEqualTo(productRequest.getPrice());
    }

    private void 상품_생성_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private ExtractableResponse<Response> 상품_등록되어_있음(ProductRequest productRequest) {
        return 상품_생성_요청(productRequest);
    }

    private ExtractableResponse<Response> 상품_목록_조회_요청() {
        return RestUtils.get(URL);
    }

    private void 상품_목록_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 상품_목록_포함됨(ExtractableResponse<Response> response, List<ExtractableResponse<Response>> createdResponses) {
        List<Long> expectedIds = createdResponses.stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[3]))
                .collect(Collectors.toList());

        List<Long> actualIds = response.jsonPath().getList(".", ProductResponse.class).stream()
                .map(ProductResponse::getId)
                .collect(Collectors.toList());

        assertThat(actualIds).containsAll(expectedIds);
    }
}
