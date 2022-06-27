package kitchenpos.table.application;

import kitchenpos.table.domain.*;
import kitchenpos.table.dto.OrderTableGroupRequest;
import kitchenpos.table.dto.TableGroupRequest;
import kitchenpos.table.dto.TableGroupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TableGroupService {
    private final OrderTableRepository orderTableRepository;
    private final TableGroupRepository tableGroupRepository;
    private final TableGroupValidator tableGroupValidator;

    public TableGroupService(final OrderTableRepository orderTableRepository,
                             final TableGroupRepository tableGroupRepository,
                             final TableGroupValidator tableGroupValidator) {
        this.orderTableRepository = orderTableRepository;
        this.tableGroupRepository = tableGroupRepository;
        this.tableGroupValidator = tableGroupValidator;
    }

    @Transactional
    public TableGroupResponse create(final TableGroupRequest tableGroupRequest) {
        final List<OrderTable> savedOrderTables = findOrderTables(tableGroupRequest.getOrderTables());
        TableGroup tableGroup = TableGroup.of(savedOrderTables, tableGroupValidator);
        return TableGroupResponse.from(tableGroupRepository.save(tableGroup));
    }

    private List<OrderTable> findOrderTables(List<OrderTableGroupRequest> orderTableGroupRequests) {
        validateIfOrderTableRequests(orderTableGroupRequests);
        List<OrderTable> orderTables = orderTableRepository.findAllByIdIn(orderTableGroupRequests.stream()
                                                                                .map(OrderTableGroupRequest::getId)
                                                                                .collect(Collectors.toList()));
        validateIfOrderTables(orderTableGroupRequests, orderTables);
        return orderTables;
    }

    private void validateIfOrderTableRequests(List<OrderTableGroupRequest> orderTableGroupRequests) {
        if (isLessOrderTables(orderTableGroupRequests)) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isLessOrderTables(List<OrderTableGroupRequest> orderTableGroupRequests) {
        return CollectionUtils.isEmpty(orderTableGroupRequests) || orderTableGroupRequests.size() < TableGroupValidator.MIN_ORDER_TABLES;
    }

    private void validateIfOrderTables(List<OrderTableGroupRequest> orderTableGroupRequests, List<OrderTable> orderTables) {
        if (orderTableGroupRequests.size() != orderTables.size()) {
            throw new IllegalArgumentException();
        }
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        TableGroup tableGroup = tableGroupRepository.findById(tableGroupId)
                .orElseThrow(IllegalArgumentException::new);
        tableGroup.ungroup(tableGroupValidator);
    }
}
