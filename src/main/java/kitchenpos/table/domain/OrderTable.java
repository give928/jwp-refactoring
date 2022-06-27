package kitchenpos.table.domain;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class OrderTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "table_group_id", foreignKey = @ForeignKey(name = "fk_order_table_table_group"))
    private TableGroup tableGroup;

    @Embedded
    private NumberOfGuests numberOfGuests;

    private boolean empty;

    protected OrderTable() {
    }

    private OrderTable(Long id, TableGroup tableGroup, int numberOfGuests, boolean empty) {
        this.id = id;
        this.tableGroup = tableGroup;
        this.numberOfGuests = NumberOfGuests.from(numberOfGuests);
        this.empty = empty;
    }

    public static OrderTable of(int numberOfGuests, boolean empty) {
        return of(null, null, numberOfGuests, empty);
    }

    public static OrderTable of(Long id, TableGroup tableGroup, int numberOfGuests, boolean empty) {
        return new OrderTable(id, tableGroup, numberOfGuests, empty);
    }

    public Long getId() {
        return id;
    }

    public TableGroup getTableGroup() {
        return tableGroup;
    }

    public int getNumberOfGuests() {
        return numberOfGuests.get();
    }

    public boolean isEmpty() {
        return empty;
    }

    public void clearTableGroup(TableValidator tableValidator) {
        tableValidator.clearTableGroup(this);
        this.tableGroup = null;
    }

    public void changeTableGroup(TableValidator tableValidator, TableGroup tableGroup) {
        tableValidator.changeTableGroup(this);
        changeEmpty(tableValidator, Boolean.FALSE);
        this.tableGroup = tableGroup;
    }

    public OrderTable changeNumberOfGuests(OrderTableValidator orderTableValidator, int numberOfGuests) {
        orderTableValidator.changeNumberOfGuests(this);
        this.numberOfGuests = NumberOfGuests.from(numberOfGuests);
        return this;
    }

    public OrderTable changeEmpty(TableValidator tableValidator, boolean empty) {
        tableValidator.changeEmpty(this);
        return changeEmpty(empty);
    }

    private OrderTable changeEmpty(boolean empty) {
        this.empty = empty;
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
        OrderTable that = (OrderTable) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public static OrderTable.OrderTableBuilder builder() {
        return new OrderTable.OrderTableBuilder();
    }

    public static class OrderTableBuilder {
        private Long id;
        private TableGroup tableGroup;
        private int numberOfGuests;
        private boolean empty;

        OrderTableBuilder() {
        }

        public OrderTable.OrderTableBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public OrderTable.OrderTableBuilder tableGroup(TableGroup tableGroup) {
            this.tableGroup = tableGroup;
            return this;
        }

        public OrderTable.OrderTableBuilder numberOfGuests(int numberOfGuests) {
            this.numberOfGuests = numberOfGuests;
            return this;
        }

        public OrderTable.OrderTableBuilder empty(boolean empty) {
            this.empty = empty;
            return this;
        }

        public OrderTable build() {
            return new OrderTable(this.id, this.tableGroup, this.numberOfGuests, this.empty);
        }
    }
}
