package kitchenpos.common.util;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;

public final class RestUtils {
    private RestUtils() {
    }

    public static <T> ResponseEntity<T> get(String url, String path, MultiValueMap<String, String> queryParams,
                                            ParameterizedTypeReference<T> responseType) {
        URI uri = UriComponentsBuilder
                .fromUriString(url)
                .path(path)
                .queryParams(queryParams)
                .encode(Charset.defaultCharset())
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(uri, HttpMethod.GET, entity, responseType);
    }
}
