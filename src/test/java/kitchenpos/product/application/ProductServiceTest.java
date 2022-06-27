package kitchenpos.product.application;

import kitchenpos.product.domain.Product;
import kitchenpos.product.domain.ProductRepository;
import kitchenpos.product.dto.ProductRequest;
import kitchenpos.product.dto.ProductResponse;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static kitchenpos.Fixtures.aProduct1;
import static kitchenpos.Fixtures.aProduct2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;

    public static Stream<BigDecimal> invalidPriceParameter() {
        return Stream.of(null, BigDecimal.valueOf(-1));
    }

    @BeforeEach
    void setUp() {
        product1 = aProduct1();
        product2 = aProduct2();
    }

    @DisplayName("상품을 등록하고 등록한 상품을 반환한다.")
    @Test
    void create() {
        // given
        ProductRequest productRequest = new ProductRequest(product1.getName(), product1.getPrice());

        given(productRepository.save(productRequest.toProduct())).willReturn(product1);

        // when
        ProductResponse productResponse = productService.create(productRequest);

        // then
        assertThat(productResponse.getId()).isEqualTo(product1.getId());
        assertThat(productResponse.getName()).isEqualTo(product1.getName());
        assertThat(productResponse.getPrice()).isEqualTo(product1.getPrice());
    }

    @DisplayName("상품의 가격은 필수로 입력해야 하고, 0원 이상만 가능하다.")
    @ParameterizedTest
    @MethodSource("invalidPriceParameter")
    void invalidPrice(BigDecimal price) {
        // given
        ProductRequest productRequest = new ProductRequest(product1.getName(), price);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> productService.create(productRequest);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        given(productRepository.findAll()).willReturn(Arrays.asList(product1, product2));

        // when
        List<ProductResponse> productResponses = productService.list();

        // then
        assertThat(productResponses).extracting("id").containsExactly(product1.getId(), product2.getId());
        assertThat(productResponses).extracting("name").containsExactly(product1.getName(), product2.getName());
        assertThat(productResponses).extracting("price").containsExactly(product1.getPrice(), product2.getPrice());
    }
}
