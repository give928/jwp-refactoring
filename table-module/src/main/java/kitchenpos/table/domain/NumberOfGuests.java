package kitchenpos.table.domain;

import kitchenpos.table.exception.InvalidNumberOfGuestsException;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

@Embeddable
public class NumberOfGuests {
    private static final int MIN = 0;
    private static final int MAX = 4;
    private static final Map<Integer, NumberOfGuests> values;

    static {
        values = new HashMap<>();
        IntStream.rangeClosed(MIN, MAX).forEach(i -> values.put(i, new NumberOfGuests(i)));
    }

    @Column(name = "number_of_guests")
    private int value;

    protected NumberOfGuests() {
    }

    private NumberOfGuests(int value) {
        this.value = value;
    }

    public static NumberOfGuests from(int value) {
        validate(value);
        if (notExistsValue(value)) {
            values.put(value, new NumberOfGuests(value));
        }
        return values.get(value);
    }

    private static boolean notExistsValue(int value) {
        return Optional.ofNullable(values.get(value)).isEmpty();
    }

    private static void validate(int numberOfGuests) {
        if (numberOfGuests < MIN) {
            throw new InvalidNumberOfGuestsException();
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
