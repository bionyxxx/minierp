package com.minierp.application.usecase;

import java.time.Instant;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.purchase.PurchaseOrder;
import com.minierp.domain.model.purchase.PurchaseOrderLine;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.shared.DocumentNumberType;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.shared.SortField;
import com.minierp.domain.model.supplier.Supplier;
import com.minierp.domain.port.in.PurchaseOrderCommand;
import com.minierp.domain.port.in.PurchaseOrderLineCommand;
import com.minierp.domain.port.in.PurchaseOrderUseCase;
import com.minierp.domain.port.out.DocumentNumberPort;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.domain.port.out.PurchaseOrderRepositoryPort;
import com.minierp.domain.port.out.SupplierRepositoryPort;
import com.minierp.domain.service.ApprovalPolicy;

/**
 * Purchase Order use cases: create, approve, reject, list and detail.
 */
@Service
public class PurchaseOrderService implements PurchaseOrderUseCase {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "poNumber", "orderDate", "status", "totalAmount", "createdAt");

    private static final SortField DEFAULT_SORT = SortField.desc("id");

    private final PurchaseOrderRepositoryPort purchaseOrderRepository;
    private final SupplierRepositoryPort supplierRepository;
    private final ItemRepositoryPort itemRepository;
    private final DocumentNumberPort documentNumbers;
    private final ApprovalPolicy approvalPolicy;

    public PurchaseOrderService(PurchaseOrderRepositoryPort purchaseOrderRepository,
            SupplierRepositoryPort supplierRepository, ItemRepositoryPort itemRepository,
            DocumentNumberPort documentNumbers, ApprovalPolicy approvalPolicy) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.itemRepository = itemRepository;
        this.documentNumbers = documentNumbers;
        this.approvalPolicy = approvalPolicy;
    }

    @Transactional
    @Override
    public PurchaseOrder create(AuthenticatedUser actor, PurchaseOrderCommand command) {
        if (command.lines().isEmpty()) {
            throw new BusinessRuleException("Purchase Order minimal memiliki 1 line");
        }
        Supplier supplier = supplierRepository.findById(command.supplierId())
                .orElseThrow(() -> new NotFoundException("Supplier", command.supplierId()));
        if (!supplier.isActive()) {
            throw new BusinessRuleException("Supplier %s tidak aktif".formatted(supplier.getName()));
        }

        PurchaseOrder po = PurchaseOrder.create(supplier, command.orderDate(), command.notes(), actor.username());
        for (PurchaseOrderLineCommand line : command.lines()) {
            Item item = itemRepository.findById(line.itemId())
                    .orElseThrow(() -> new NotFoundException("Item", line.itemId()));
            if (!item.isActive()) {
                throw new BusinessRuleException("Item %s tidak aktif".formatted(item.getSku()));
            }
            po.addLine(PurchaseOrderLine.create(item, line.qty(), line.unitPrice()));
        }

        // The number must be assigned before save: Hibernate snapshots entity state
        // at persist time, so post-save changes are not part of the initial INSERT.
        po.assignNumber(documentNumbers.nextNumber(DocumentNumberType.PURCHASE_ORDER));
        return purchaseOrderRepository.save(po);
    }

    @Transactional(readOnly = true)
    @Override
    public PurchaseOrder get(AuthenticatedUser actor, Long id) {
        return findOrThrow(id);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<PurchaseOrder> list(AuthenticatedUser actor, DocumentStatus status, Long supplierId,
            PageQuery query) {
        return purchaseOrderRepository.findPage(status, supplierId,
                Pagination.normalize(query, SORT_PROPERTIES, DEFAULT_SORT));
    }

    @Transactional
    @Override
    public PurchaseOrder approve(AuthenticatedUser actor, Long id) {
        PurchaseOrder po = findOrThrow(id);
        approvalPolicy.checkCanApprove(actor, po.getTotalAmount(), "Purchase Order " + po.getPoNumber());
        po.approve(actor.username(), Instant.now());
        purchaseOrderRepository.save(po);
        return po;
    }

    @Transactional
    @Override
    public PurchaseOrder reject(AuthenticatedUser actor, Long id, String reason) {
        PurchaseOrder po = findOrThrow(id);
        approvalPolicy.checkCanApprove(actor, po.getTotalAmount(), "Purchase Order " + po.getPoNumber());
        po.reject(reason);
        purchaseOrderRepository.save(po);
        return po;
    }

    private PurchaseOrder findOrThrow(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase Order", id));
    }
}
