package kitchenpos.table.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class TableGroup extends AbstractAggregateRoot<TableGroup> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Embedded
    private OrderTables orderTables;

    public TableGroup() {
    }

    protected TableGroup(Long id, OrderTables orderTables) {
        this.id = id;
        this.orderTables = orderTables;
    }

    private TableGroup(Long id, List<OrderTable> orderTables, TableGroupValidator tableGroupValidator) {
        this(id, OrderTables.of(Optional.ofNullable(orderTables)
                                        .orElse(new ArrayList<>())));
        tableGroupValidator.create(this);
        this.orderTables.group(this);
    }

    public static TableGroup of(List<OrderTable> orderTables, TableGroupValidator tableGroupValidator) {
        return of(null, orderTables, tableGroupValidator);
    }

    public static TableGroup of(Long id, List<OrderTable> orderTables, TableGroupValidator tableGroupValidator) {
        return new TableGroup(id, orderTables, tableGroupValidator);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<OrderTable> getOrderTables() {
        return orderTables.get();
    }

    public TableGroup group(List<OrderTable> orderTables) {
        this.orderTables = OrderTables.of(orderTables);
        this.orderTables.group(this);
        return this;
    }

    public TableGroup ungroup() {
        registerEvent(TableUngroupedEvent.from(this));
        orderTables.ungroup();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableGroup that = (TableGroup) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "TableGroup{" +
                "id=" + id +
                ", createdDate=" + createdDate +
                ", orderTables=" + orderTables +
                '}';
    }
}
