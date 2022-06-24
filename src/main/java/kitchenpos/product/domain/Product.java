package kitchenpos.product.domain;

import kitchenpos.common.domain.Name;
import kitchenpos.common.domain.Price;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Name name;

    @Embedded
    private Price price;

    protected Product() {
    }

    private Product(Long id, Name name, Price price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static Product of(Name name, Price price) {
        return of(null, name, price);
    }

    public static Product of(Long id, Name name, Price price) {
        return new Product(id, name, price);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public BigDecimal getPrice() {
        return price.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Product product = (Product) o;
        return Objects.equals(getId(), product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
