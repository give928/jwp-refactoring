package kitchenpos.common.utils;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public final class RestUtils {
    private RestUtils() {
    }

    public static ExtractableResponse<Response> get(String path) {
        return RestAssured.given().log().all()
                .when().get(path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> post(String path, Object params) {
        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post(path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> put(String path, Object params) {
        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put(path)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> delete(String path) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().delete(path)
                .then().log().all()
                .extract();
    }

    public static HttpMethod method(String name) {
        if ("POST".equalsIgnoreCase(name)) {
            return HttpMethod.POST;
        }
        if ("PUT".equalsIgnoreCase(name)) {
            return HttpMethod.PUT;
        }
        if ("DELETE".equalsIgnoreCase(name)) {
            return HttpMethod.DELETE;
        }
        return HttpMethod.GET;
    }

    public static HttpStatus status(String name) {
        if ("POST".equalsIgnoreCase(name)) {
            return HttpStatus.CREATED;
        }
        if ("DELETE".equalsIgnoreCase(name)) {
            return HttpStatus.NO_CONTENT;
        }
        return HttpStatus.OK;
    }
}
