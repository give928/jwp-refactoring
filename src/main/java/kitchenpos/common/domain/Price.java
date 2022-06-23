package kitchenpos.common.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Price {
    @Column(nullable = false)
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
            throw new IllegalArgumentException();
        }
    }

    public BigDecimal get() {
        return value;
    }
}
