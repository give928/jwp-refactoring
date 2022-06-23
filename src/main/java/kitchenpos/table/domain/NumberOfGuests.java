package kitchenpos.table.domain;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class NumberOfGuests {
    private int value;

    protected NumberOfGuests() {
    }

    private NumberOfGuests(int value) {
        this.value = value;
    }

    public static NumberOfGuests from(int value) {
        validate(value);
        return new NumberOfGuests(value);
    }

    private static void validate(int numberOfGuests) {
        if (numberOfGuests < 0) {
            throw new IllegalArgumentException();
        }
    }

    public int get() {
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
        NumberOfGuests that = (NumberOfGuests) o;
        return get() == that.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(get());
    }
}
