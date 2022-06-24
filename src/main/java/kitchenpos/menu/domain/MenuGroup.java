package kitchenpos.menu.domain;

import kitchenpos.common.domain.Name;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class MenuGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Name name;

    protected MenuGroup() {
    }

    private MenuGroup(Long id, Name name) {
        this.id = id;
        this.name = name;
    }

    public static MenuGroup of(Name name) {
        return of(null, name);
    }

    public static MenuGroup of(Long id, Name name) {
        return new MenuGroup(id, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MenuGroup menuGroup = (MenuGroup) o;
        return Objects.equals(getId(), menuGroup.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
