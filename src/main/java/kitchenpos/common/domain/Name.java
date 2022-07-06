package kitchenpos.common.domain;

import kitchenpos.common.exception.RequiredNameException;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.Optional;

@Embeddable
public class Name {
    @Column(name = "name", nullable = false)
    private String value;

    protected Name() {
    }

    private Name(String value) {
        this.value = validateIfEmptyName(value);
    }

    public static Name from(String value) {
        return new Name(value);
    }

    private String validateIfEmptyName(String value) {
        return Optional.ofNullable(value)
                .map(StringUtils::trimWhitespace)
                .filter(StringUtils::hasText)
                .orElseThrow(RequiredNameException::new);
    }

    public String get() {
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
        Name name = (Name) o;
        return Objects.equals(get(), name.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get());
    }

    @Override
    public String toString() {
        return get();
    }
}
