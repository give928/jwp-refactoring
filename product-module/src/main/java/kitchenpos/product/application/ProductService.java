package kitchenpos.product.application;

import kitchenpos.product.domain.ProductRepository;
import kitchenpos.product.dto.ProductRequest;
import kitchenpos.product.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse create(final ProductRequest productRequest) {
        return ProductResponse.from(productRepository.save(productRequest.toProduct()));
    }

    public List<ProductResponse> list(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return list();
        }
        return listByIdIn(ids);
    }

    private List<ProductResponse> list() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    private List<ProductResponse> listByIdIn(List<Long> ids) {
        return productRepository.findByIdIn(ids).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }
}
