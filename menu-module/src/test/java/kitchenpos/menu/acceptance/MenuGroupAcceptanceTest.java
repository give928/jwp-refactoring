package kitchenpos.menu.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import kitchenpos.common.AcceptanceTest;
import kitchenpos.menu.dto.MenuGroupRequest;
import kitchenpos.menu.dto.MenuGroupResponse;
import kitchenpos.common.utils.RestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("메뉴 그룹 관련 기능")
public class MenuGroupAcceptanceTest extends AcceptanceTest {
    private static final String URL = "/api/menu-groups";
    public static MenuGroupRequest 메뉴그룹1_요청 = new MenuGroupRequest("메뉴그룹1");
    public static MenuGroupRequest 메뉴그룹2_요청 = new MenuGroupRequest("메뉴그룹2");

    @DisplayName("메뉴 그룹을 등록하고 등록한 메뉴 그룹을 반환한다.")
    @Test
    void create() {
        // when
        ExtractableResponse<Response> 메뉴_그룹_생성_응답 = 메뉴_그룹_생성_요청(메뉴그룹1_요청);

        // then
        메뉴_그룹_생성됨(메뉴_그룹_생성_응답, 메뉴그룹1_요청);
    }

    @DisplayName("메뉴 그룹의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        ExtractableResponse<Response> 메뉴그룹1_응답 = 메뉴_그룹_등록되어_있음(메뉴그룹1_요청);
        ExtractableResponse<Response> 메뉴그룹2_응답 = 메뉴_그룹_등록되어_있음(메뉴그룹2_요청);

        // when
        ExtractableResponse<Response> 메뉴_그룹_목록_조회_응답 = 메뉴_그룹_목록_조회_요청();

        // then
        메뉴_그룹_목록_응답됨(메뉴_그룹_목록_조회_응답);
        메뉴_그룹_목록_포함됨(메뉴_그룹_목록_조회_응답, Arrays.asList(메뉴그룹1_응답, 메뉴그룹2_응답));
    }

    public static ExtractableResponse<Response> 메뉴_그룹_생성_요청(MenuGroupRequest menuGroupRequest) {
        return RestUtils.post(URL, menuGroupRequest);
    }

    private void 메뉴_그룹_생성됨(ExtractableResponse<Response> response, MenuGroupRequest menuGroupRequest) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();

        MenuGroupResponse menuGroupResponse = response.as(MenuGroupResponse.class);
        assertThat(menuGroupResponse.getId()).isNotNull();
        assertThat(menuGroupResponse.getName()).isEqualTo(menuGroupRequest.getName());
    }

    private ExtractableResponse<Response> 메뉴_그룹_등록되어_있음(MenuGroupRequest menuGroupRequest) {
        return 메뉴_그룹_생성_요청(menuGroupRequest);
    }

    private ExtractableResponse<Response> 메뉴_그룹_목록_조회_요청() {
        return RestUtils.get(URL);
    }

    private void 메뉴_그룹_목록_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 메뉴_그룹_목록_포함됨(ExtractableResponse<Response> response, List<ExtractableResponse<Response>> createdResponses) {
        List<Long> expectedIds = createdResponses.stream()
                .map(it -> Long.parseLong(it.header("Location").split("/")[3]))
                .collect(Collectors.toList());

        List<Long> actualIds = response.jsonPath().getList(".", MenuGroupResponse.class).stream()
                .map(MenuGroupResponse::getId)
                .collect(Collectors.toList());

        assertThat(actualIds).containsAll(expectedIds);
    }
}
