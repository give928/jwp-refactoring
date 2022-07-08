package kitchenpos.table.application;

import kitchenpos.table.domain.*;
import kitchenpos.table.dto.OrderTableGroupRequest;
import kitchenpos.table.dto.TableGroupRequest;
import kitchenpos.table.dto.TableGroupResponse;
import kitchenpos.table.exception.TableGroupNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TableGroupService {
    private final OrderTableRepository orderTableRepository;
    private final TableGroupRepository tableGroupRepository;
    private final TableGroupValidator tableGroupValidator;
    private final TableEventPublisher tableEventPublisher;

    public TableGroupService(final OrderTableRepository orderTableRepository,
                             final TableGroupRepository tableGroupRepository,
                             final TableGroupValidator tableGroupValidator,
                             final TableEventPublisher tableEventPublisher) {
        this.orderTableRepository = orderTableRepository;
        this.tableGroupRepository = tableGroupRepository;
        this.tableGroupValidator = tableGroupValidator;
        this.tableEventPublisher = tableEventPublisher;
    }

    @Transactional
    public TableGroupResponse create(final TableGroupRequest tableGroupRequest) {
        final List<OrderTable> savedOrderTables = findOrderTables(tableGroupRequest.getOrderTables());
        TableGroup tableGroup = TableGroup.of(savedOrderTables, tableGroupValidator);
        return TableGroupResponse.from(tableGroupRepository.save(tableGroup));
    }

    private List<OrderTable> findOrderTables(List<OrderTableGroupRequest> orderTableGroupRequests) {
        List<Long> orderTableIds = mapOrderTableIds(orderTableGroupRequests);
        tableGroupValidator.validateIfLessOrderTables(orderTableIds);
        List<OrderTable> orderTables = orderTableRepository.findAllByIdIn(orderTableIds);
        tableGroupValidator.validateIfNotFoundOrderTables(orderTableIds, orderTables);
        return orderTables;
    }

    private List<Long> mapOrderTableIds(List<OrderTableGroupRequest> orderTableGroupRequests) {
        return Optional.ofNullable(orderTableGroupRequests)
                .orElse(Collections.emptyList())
                .stream()
                .map(OrderTableGroupRequest::getId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        TableGroup tableGroup = tableGroupRepository.findById(tableGroupId)
                .orElseThrow(TableGroupNotFoundException::new);
        tableGroupRepository.save(tableGroup.ungroup(tableEventPublisher));
    }
}
