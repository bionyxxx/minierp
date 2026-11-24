package com.minierp.purchase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.minierp.inventory.StockMovementRef;
import com.minierp.inventory.StockService;
import com.minierp.item.Item;
import com.minierp.item.ItemRepository;
import com.minierp.purchase.dto.GoodsReceiptLineRequest;
import com.minierp.purchase.dto.GoodsReceiptRequest;
import com.minierp.purchase.dto.GoodsReceiptResponse;
import com.minierp.purchase.dto.RejectRequest;
import com.minierp.warehouse.Warehouse;
import com.minierp.warehouse.WarehouseRepository;

/**
 * Application service for Goods Receipt (penerimaan barang) use cases.
 */
@Service
public class GoodsReceiptService {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "grnNumber", "receiptDate", "status", "createdAt");

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final StockService stockService;
    private final ApprovalPolicy approvalPolicy;

    public GoodsReceiptService(GoodsReceiptRepository goodsReceiptRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            WarehouseRepository warehouseRepository,
            ItemRepository itemRepository,
            StockService stockService,
            ApprovalPolicy approvalPolicy) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
        this.stockService = stockService;
        this.approvalPolicy = approvalPolicy;
    }

    @Transactional
    public GoodsReceiptResponse create(AuthenticatedUser actor, GoodsReceiptRequest request) {
        PurchaseOrder po = purchaseOrderRepository.findById(request.purchaseOrderId())
                .orElseThrow(() -> new NotFoundException("Purchase Order", request.purchaseOrderId()));
        if (po.getStatus() != DocumentStatus.APPROVED) {
            throw new InvalidStateException("GRN hanya dapat dibuat untuk Purchase Order yang sudah disetujui");
        }
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse", request.warehouseId()));
        if (!warehouse.isActive()) {
            throw new BusinessRuleException("Warehouse %s tidak aktif".formatted(warehouse.getName()));
        }
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new BusinessRuleException("Goods Receipt minimal memiliki 1 line");
        }

        // Resolve items and validate that every line belongs to the Purchase Order.
        List<Item> resolvedItems = new ArrayList<>();
        Map<Long, BigDecimal> requestedQty = new LinkedHashMap<>();
        Map<Long, Item> itemById = new LinkedHashMap<>();
        for (GoodsReceiptLineRequest line : request.lines()) {
            if (line.qty() == null || line.qty().signum() <= 0) {
                throw new BusinessRuleException("qty line Goods Receipt harus lebih dari 0");
            }
            Item item = itemRepository.findById(line.itemId())
                    .orElseThrow(() -> new NotFoundException("Item", line.itemId()));
            if (po.getLines().stream().noneMatch(l -> Objects.equals(l.getItem().getId(), item.getId()))) {
                throw new BusinessRuleException("Item %s tidak ada dalam Purchase Order".formatted(item.getSku()));
            }
            resolvedItems.add(item);
            itemById.put(item.getId(), item);
            requestedQty.merge(item.getId(), line.qty(), BigDecimal::add);
        }

        // Ordered qty per item on the Purchase Order.
        Map<Long, BigDecimal> orderedQty = new HashMap<>();
        for (PurchaseOrderLine poLine : po.getLines()) {
            orderedQty.merge(poLine.getItem().getId(), poLine.getQty(), BigDecimal::add);
        }

        // Cumulative qty already received per item: sum over the lines of this
        // PO's GRNs that are still counted (PENDING or APPROVED). A REJECTED GRN
        // does not count, so the receipt can be re-created after a rejection.
        Map<Long, BigDecimal> receivedQty = new HashMap<>();
        for (GoodsReceipt existing : goodsReceiptRepository.findByPurchaseOrderId(po.getId())) {
            if (existing.getStatus() == DocumentStatus.PENDING || existing.getStatus() == DocumentStatus.APPROVED) {
                for (GoodsReceiptLine line : existing.getLines()) {
                    receivedQty.merge(line.getItem().getId(), line.getQty(), BigDecimal::add);
                }
            }
        }

        for (Map.Entry<Long, BigDecimal> entry : requestedQty.entrySet()) {
            BigDecimal ordered = orderedQty.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            BigDecimal received = receivedQty.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            if (received.add(entry.getValue()).compareTo(ordered) > 0) {
                Item item = itemById.get(entry.getKey());
                throw new BusinessRuleException(
                        "Qty penerimaan untuk item %s melebihi qty pesanan".formatted(item.getSku()));
            }
        }

        GoodsReceipt grn = new GoodsReceipt(po, warehouse, request.receiptDate());
        grn.setNotes(request.notes());
        grn.setCreatedBy(actor.username());
        List<GoodsReceiptLineRequest> lineRequests = request.lines();
        for (int i = 0; i < lineRequests.size(); i++) {
            grn.addLine(new GoodsReceiptLine(resolvedItems.get(i), lineRequests.get(i).qty()));
        }

        // The number must be set before save: Hibernate snapshots entity state at
        // persist time, so post-save changes are not part of the initial INSERT.
        grn.setGrnNumber(DocumentNumbers.format("GRN", goodsReceiptRepository.nextNumberValue()));
        goodsReceiptRepository.save(grn);
        return PurchaseMapper.toResponse(grn);
    }

    @Transactional(readOnly = true)
    public GoodsReceiptResponse get(AuthenticatedUser actor, Long id) {
        return PurchaseMapper.toResponse(findOne(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<GoodsReceiptResponse> list(AuthenticatedUser actor, Long purchaseOrderId, DocumentStatus status,
            Pageable pageable) {
        Pageable effective = Pagination.normalize(pageable, SORT_PROPERTIES, Sort.by(Sort.Order.desc("id")));
        return PageResponse.from(
                goodsReceiptRepository.findPage(purchaseOrderId, status, effective).map(PurchaseMapper::toResponse));
    }

    @Transactional
    public GoodsReceiptResponse approve(AuthenticatedUser actor, Long id) {
        GoodsReceipt grn = findOne(id);
        // The PO total is the approval basis for the receipt document.
        approvalPolicy.checkCanApprove(actor, grn.getPurchaseOrder().getTotalAmount(),
                "Goods Receipt " + grn.getGrnNumber());
        requirePending(grn);
        grn.setStatus(DocumentStatus.APPROVED);
        grn.setApprovedBy(actor.username());
        grn.setApprovedAt(Instant.now());
        goodsReceiptRepository.save(grn);
        for (GoodsReceiptLine line : grn.getLines()) {
            stockService.increase(line.getItem(), grn.getWarehouse(), line.getQty(),
                    StockMovementRef.GRN, grn.getId(), grn.getGrnNumber(),
                    "Penerimaan barang", actor.username());
        }
        return PurchaseMapper.toResponse(grn);
    }

    @Transactional
    public GoodsReceiptResponse reject(AuthenticatedUser actor, Long id, RejectRequest request) {
        GoodsReceipt grn = findOne(id);
        approvalPolicy.checkCanApprove(actor, grn.getPurchaseOrder().getTotalAmount(),
                "Goods Receipt " + grn.getGrnNumber());
        requirePending(grn);
        grn.setStatus(DocumentStatus.REJECTED);
        grn.setRejectReason(request.reason());
        goodsReceiptRepository.save(grn);
        // Rejection never changes stock; the Purchase Order stays APPROVED.
        return PurchaseMapper.toResponse(grn);
    }

    private GoodsReceipt findOne(Long id) {
        return goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Goods Receipt", id));
    }

    private void requirePending(GoodsReceipt grn) {
        if (grn.getStatus() != DocumentStatus.PENDING) {
            throw new InvalidStateException("hanya Goods Receipt berstatus PENDING yang dapat diproses");
        }
    }
}
