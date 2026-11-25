package com.minierp.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.customer.Customer;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.sales.SalesOrder;
import com.minierp.domain.model.sales.SalesOrderLine;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.shared.DocumentNumberType;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.shared.SortField;
import com.minierp.domain.port.in.SalesOrderCommand;
import com.minierp.domain.port.in.SalesOrderLineCommand;
import com.minierp.domain.port.in.SalesOrderUseCase;
import com.minierp.domain.port.out.CustomerRepositoryPort;
import com.minierp.domain.port.out.DocumentNumberPort;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.domain.port.out.SalesOrderRepositoryPort;
import com.minierp.domain.service.ApprovalPolicy;

/**
 * Sales Order use cases: create, approve, reject, list and detail.
 */
@Service
public class SalesOrderService implements SalesOrderUseCase {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "soNumber", "orderDate", "status", "totalAmount", "createdAt");

    private static final SortField DEFAULT_SORT = SortField.desc("id");

    private final SalesOrderRepositoryPort salesOrderRepository;
    private final CustomerRepositoryPort customerRepository;
    private final ItemRepositoryPort itemRepository;
    private final DocumentNumberPort documentNumbers;
    private final ApprovalPolicy approvalPolicy;

    public SalesOrderService(SalesOrderRepositoryPort salesOrderRepository,
            CustomerRepositoryPort customerRepository, ItemRepositoryPort itemRepository,
            DocumentNumberPort documentNumbers, ApprovalPolicy approvalPolicy) {
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
        this.itemRepository = itemRepository;
        this.documentNumbers = documentNumbers;
        this.approvalPolicy = approvalPolicy;
    }

    @Transactional
    @Override
    public SalesOrder create(AuthenticatedUser actor, SalesOrderCommand command) {
        if (command.lines().isEmpty()) {
            throw new BusinessRuleException("Sales Order minimal memiliki 1 line");
        }
        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new NotFoundException("Customer", command.customerId()));
        if (!customer.isActive()) {
            throw new BusinessRuleException("Customer %s tidak aktif".formatted(customer.getName()));
        }

        SalesOrder so = SalesOrder.create(customer, command.orderDate(), command.notes(), actor.username());
        for (SalesOrderLineCommand line : command.lines()) {
            so.addLine(SalesOrderLine.create(resolveItem(line.itemId()), line.qty(), line.unitPrice()));
        }

        // The number must be assigned before save: Hibernate snapshots entity state
        // at persist time, so post-save changes are not part of the initial INSERT.
        so.assignNumber(documentNumbers.nextNumber(DocumentNumberType.SALES_ORDER));
        return salesOrderRepository.save(so);
    }

    @Transactional(readOnly = true)
    @Override
    public SalesOrder get(AuthenticatedUser actor, Long id) {
        return findOrThrow(id);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<SalesOrder> list(AuthenticatedUser actor, DocumentStatus status, Long customerId,
            PageQuery query) {
        return salesOrderRepository.findPage(status, customerId,
                Pagination.normalize(query, SORT_PROPERTIES, DEFAULT_SORT));
    }

    @Transactional
    @Override
    public SalesOrder approve(AuthenticatedUser actor, Long id) {
        SalesOrder so = findOrThrow(id);
        approvalPolicy.checkCanApprove(actor, so.getTotalAmount(), "Sales Order " + so.getSoNumber());
        so.approve(actor.username(), Instant.now());
        salesOrderRepository.save(so);
        return so;
    }

    @Transactional
    @Override
    public SalesOrder reject(AuthenticatedUser actor, Long id, String reason) {
        SalesOrder so = findOrThrow(id);
        approvalPolicy.checkCanApprove(actor, so.getTotalAmount(), "Sales Order " + so.getSoNumber());
        so.reject(reason);
        salesOrderRepository.save(so);
        return so;
    }

    private SalesOrder findOrThrow(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sales Order", id));
    }

    private Item resolveItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item", itemId));
        if (!item.isActive()) {
            throw new BusinessRuleException("Item %s tidak aktif".formatted(item.getSku()));
        }
        return item;
    }
}
