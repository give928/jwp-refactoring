package kitchenpos.application;

import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Product;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductService productService;

    private Product product1;

    public static Stream<BigDecimal> invalidPriceParameter() {
        return Stream.of(null, BigDecimal.valueOf(-1));
    }

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "음식1", BigDecimal.ONE);
    }

    @DisplayName("상품을 등록하고 등록한 상품을 반환한다.")
    @Test
    void create() {
        // given
        Product product = new Product(product1.getName(), product1.getPrice());

        given(productDao.save(product)).willReturn(product1);

        // when
        Product savedProduct = productService.create(product);

        // then
        assertThat(savedProduct).isEqualTo(product1);
    }

    @DisplayName("상품의 가격은 필수로 입력해야 하고, 0원 이상만 가능하다.")
    @ParameterizedTest
    @MethodSource("invalidPriceParameter")
    void invalidPrice(BigDecimal price) {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> productService.create(new Product("음식1", price));

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품의 전체 목록을 조회한다.")
    @Test
    void list() {
        // given
        List<Product> products = Arrays.asList(product1, new Product(2L, "음식2", BigDecimal.ONE));

        given(productDao.findAll()).willReturn(products);

        // when
        List<Product> findProducts = productService.list();

        // then
        assertThat(findProducts).containsExactlyElementsOf(products);
    }
}
