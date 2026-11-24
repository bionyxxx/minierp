package com.minierp.purchase;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.approval.ApprovalPolicy;
import com.minierp.common.AuthenticatedUser;
import com.minierp.common.BusinessRuleException;
import com.minierp.common.DocumentNumbers;
import com.minierp.common.DocumentStatus;
import com.minierp.common.InvalidStateException;
import com.minierp.common.NotFoundException;
import com.minierp.common.PageResponse;
import com.minierp.common.Pagination;
import com.minierp.item.Item;
import com.minierp.item.ItemRepository;
import com.minierp.purchase.dto.PurchaseOrderLineRequest;
import com.minierp.purchase.dto.PurchaseOrderRequest;
import com.minierp.purchase.dto.PurchaseOrderResponse;
import com.minierp.purchase.dto.RejectRequest;
import com.minierp.supplier.Supplier;
import com.minierp.supplier.SupplierRepository;

/**
 * Application service for Purchase Order use cases.
 */
@Service
public class PurchaseOrderService {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "poNumber", "orderDate", "status", "totalAmount", "createdAt");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private final ApprovalPolicy approvalPolicy;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ItemRepository itemRepository,
            ApprovalPolicy approvalPolicy) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.itemRepository = itemRepository;
        this.approvalPolicy = approvalPolicy;
    }

    @Transactional
    public PurchaseOrderResponse create(AuthenticatedUser actor, PurchaseOrderRequest request) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new BusinessRuleException("Purchase Order minimal memiliki 1 line");
        }
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new NotFoundException("Supplier", request.supplierId()));
        if (!supplier.isActive()) {
            throw new BusinessRuleException("Supplier %s tidak aktif".formatted(supplier.getName()));
        }

        PurchaseOrder po = new PurchaseOrder(supplier, request.orderDate());
        po.setNotes(request.notes());
        po.setCreatedBy(actor.username());

        for (PurchaseOrderLineRequest line : request.lines()) {
            if (line.qty() == null || line.qty().signum() <= 0) {
                throw new BusinessRuleException("qty line Purchase Order harus lebih dari 0");
            }
            if (line.unitPrice() == null || line.unitPrice().signum() < 0) {
                throw new BusinessRuleException("unitPrice line Purchase Order tidak boleh negatif");
            }
            Item item = itemRepository.findById(line.itemId())
                    .orElseThrow(() -> new NotFoundException("Item", line.itemId()));
            if (!item.isActive()) {
                throw new BusinessRuleException("Item %s tidak aktif".formatted(item.getSku()));
            }
            po.addLine(new PurchaseOrderLine(item, line.qty(), line.unitPrice()));
        }

        // The number must be set before save: Hibernate snapshots entity state at
        // persist time, so post-save changes are not part of the initial INSERT.
        po.setPoNumber(DocumentNumbers.format("PO", purchaseOrderRepository.nextNumberValue()));
        purchaseOrderRepository.save(po);
        return PurchaseMapper.toResponse(po);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse get(AuthenticatedUser actor, Long id) {
        return PurchaseMapper.toResponse(findOne(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderResponse> list(AuthenticatedUser actor, DocumentStatus status, Long supplierId,
            Pageable pageable) {
        Pageable effective = Pagination.normalize(pageable, SORT_PROPERTIES, Sort.by(Sort.Order.desc("id")));
        return PageResponse.from(
                purchaseOrderRepository.findPage(status, supplierId, effective).map(PurchaseMapper::toResponse));
    }

    @Transactional
    public PurchaseOrderResponse approve(AuthenticatedUser actor, Long id) {
        PurchaseOrder po = findOne(id);
        approvalPolicy.checkCanApprove(actor, po.getTotalAmount(), "Purchase Order " + po.getPoNumber());
        requirePending(po);
        po.setStatus(DocumentStatus.APPROVED);
        po.setApprovedBy(actor.username());
        po.setApprovedAt(Instant.now());
        purchaseOrderRepository.save(po);
        return PurchaseMapper.toResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse reject(AuthenticatedUser actor, Long id, RejectRequest request) {
        PurchaseOrder po = findOne(id);
        approvalPolicy.checkCanApprove(actor, po.getTotalAmount(), "Purchase Order " + po.getPoNumber());
        requirePending(po);
        po.setStatus(DocumentStatus.REJECTED);
        po.setRejectReason(request.reason());
        purchaseOrderRepository.save(po);
        return PurchaseMapper.toResponse(po);
    }

    private PurchaseOrder findOne(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase Order", id));
    }

    private void requirePending(PurchaseOrder po) {
        if (po.getStatus() != DocumentStatus.PENDING) {
            throw new InvalidStateException("hanya Purchase Order berstatus PENDING yang dapat diproses");
        }
    }
}
