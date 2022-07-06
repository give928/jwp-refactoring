package kitchenpos.common.domain;

import kitchenpos.common.exception.RequiredPriceException;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Price {
    @Column(name = "price", nullable = false)
    private BigDecimal value;

    protected Price() {
    }

    private Price(BigDecimal value) {
        validate(value);
        this.value = value;
    }

    public static Price from(BigDecimal value) {
        return new Price(value);
    }

    private static void validate(BigDecimal value) {
        if (Objects.isNull(value) || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new RequiredPriceException();
        }
    }

    public BigDecimal get() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Price price = (Price) o;
        return Objects.equals(get(), price.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get());
    }
}
