package kitchenpos.table.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class TableGroup {
    public static final int MIN_ORDER_TABLES = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "tableGroup", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<OrderTable> orderTables = new ArrayList<>();

    public TableGroup() {
    }

    private TableGroup(Long id, List<OrderTable> orderTables) {
        validateOrderTables(orderTables);
        this.id = id;
        this.orderTables = orderTables;
        this.orderTables.forEach(orderTable -> orderTable.changeTableGroup(this));
    }

    public static TableGroup of(List<OrderTable> orderTables) {
        return of(null, orderTables);
    }

    public static TableGroup of(Long id, List<OrderTable> orderTables) {
        return new TableGroup(id, orderTables);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<OrderTable> getOrderTables() {
        return orderTables;
    }

    public void ungroup() {
        orderTables.forEach(OrderTable::clearTableGroup);
    }

    private void validateOrderTables(List<OrderTable> orderTables) {
        if (isLessOrderTables(orderTables)) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isLessOrderTables(List<OrderTable> orderTables) {
        return CollectionUtils.isEmpty(orderTables) || orderTables.size() < MIN_ORDER_TABLES;
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
}
