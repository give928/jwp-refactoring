package kitchenpos.common.utils;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public final class TestRestUtils {
    private TestRestUtils() {
    }

    public static <T> ResponseEntity<T> request(String requestBody, String responseBody, String url, String method, ParameterizedTypeReference<T> responseType) {
        RestTemplate restTemplate = makeRestTemplate(url, method, responseBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange(url, RestUtils.method(method), entity, responseType);
    }

    private static RestTemplate makeRestTemplate(String url, String method, String responseBody) {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
        try {
            mockRestServiceServer.expect(ExpectedCount.once(),
                                         requestTo(new URI(url)))
                    .andExpect(method(RestUtils.method(method)))
                    .andRespond(withStatus(RestUtils.status(method))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(responseBody));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        return restTemplate;
    }
}
