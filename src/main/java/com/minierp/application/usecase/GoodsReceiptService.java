package com.minierp.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.inventory.StockMovementRef;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.purchase.GoodsReceipt;
import com.minierp.domain.model.purchase.GoodsReceiptLine;
import com.minierp.domain.model.purchase.PurchaseOrder;
import com.minierp.domain.model.purchase.PurchaseOrderLine;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.shared.DocumentNumberType;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.InvalidStateException;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.shared.SortField;
import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.domain.port.in.GoodsReceiptCommand;
import com.minierp.domain.port.in.GoodsReceiptLineCommand;
import com.minierp.domain.port.in.GoodsReceiptUseCase;
import com.minierp.domain.port.out.DocumentNumberPort;
import com.minierp.domain.port.out.GoodsReceiptRepositoryPort;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.domain.port.out.PurchaseOrderRepositoryPort;
import com.minierp.domain.port.out.StockPort;
import com.minierp.domain.port.out.WarehouseRepositoryPort;
import com.minierp.domain.service.ApprovalPolicy;

/**
 * Goods Receipt (penerimaan barang) use cases. Approving a receipt increases
 * stock through the {@link StockPort}; rejection never changes stock.
 */
@Service
public class GoodsReceiptService implements GoodsReceiptUseCase {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "grnNumber", "receiptDate", "status", "createdAt");

    private static final SortField DEFAULT_SORT = SortField.desc("id");

    private final GoodsReceiptRepositoryPort goodsReceiptRepository;
    private final PurchaseOrderRepositoryPort purchaseOrderRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final ItemRepositoryPort itemRepository;
    private final StockPort stockPort;
    private final DocumentNumberPort documentNumbers;
    private final ApprovalPolicy approvalPolicy;

    public GoodsReceiptService(GoodsReceiptRepositoryPort goodsReceiptRepository,
            PurchaseOrderRepositoryPort purchaseOrderRepository, WarehouseRepositoryPort warehouseRepository,
            ItemRepositoryPort itemRepository, StockPort stockPort, DocumentNumberPort documentNumbers,
            ApprovalPolicy approvalPolicy) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
        this.stockPort = stockPort;
        this.documentNumbers = documentNumbers;
        this.approvalPolicy = approvalPolicy;
    }

    @Transactional
    @Override
    public GoodsReceipt create(AuthenticatedUser actor, GoodsReceiptCommand command) {
        PurchaseOrder po = purchaseOrderRepository.findById(command.purchaseOrderId())
                .orElseThrow(() -> new NotFoundException("Purchase Order", command.purchaseOrderId()));
        if (po.getStatus() != DocumentStatus.APPROVED) {
            throw new InvalidStateException("GRN hanya dapat dibuat untuk Purchase Order yang sudah disetujui");
        }
        Warehouse warehouse = warehouseRepository.findById(command.warehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse", command.warehouseId()));
        if (!warehouse.isActive()) {
            throw new BusinessRuleException("Warehouse %s tidak aktif".formatted(warehouse.getName()));
        }
        if (command.lines().isEmpty()) {
            throw new BusinessRuleException("Goods Receipt minimal memiliki 1 line");
        }

        // Resolve items and validate that every line belongs to the Purchase Order.
        List<GoodsReceiptLine> receivedLines = new ArrayList<>();
        Map<Long, BigDecimal> requestedQty = new LinkedHashMap<>();
        Map<Long, Item> itemById = new LinkedHashMap<>();
        for (GoodsReceiptLineCommand line : command.lines()) {
            Item item = itemRepository.findById(line.itemId())
                    .orElseThrow(() -> new NotFoundException("Item", line.itemId()));
            GoodsReceiptLine receivedLine = GoodsReceiptLine.create(item, line.qty());
            if (po.getLines().stream().noneMatch(l -> Objects.equals(l.getItem().getId(), item.getId()))) {
                throw new BusinessRuleException("Item %s tidak ada dalam Purchase Order".formatted(item.getSku()));
            }
            receivedLines.add(receivedLine);
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

        GoodsReceipt grn = GoodsReceipt.create(po, warehouse, command.receiptDate(), command.notes(),
                actor.username());
        for (GoodsReceiptLine line : receivedLines) {
            grn.addLine(line);
        }

        // The number must be assigned before save: Hibernate snapshots entity state
        // at persist time, so post-save changes are not part of the initial INSERT.
        grn.assignNumber(documentNumbers.nextNumber(DocumentNumberType.GOODS_RECEIPT));
        return goodsReceiptRepository.save(grn);
    }

    @Transactional(readOnly = true)
    @Override
    public GoodsReceipt get(AuthenticatedUser actor, Long id) {
        return findOrThrow(id);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<GoodsReceipt> list(AuthenticatedUser actor, Long purchaseOrderId, DocumentStatus status,
            PageQuery query) {
        return goodsReceiptRepository.findPage(purchaseOrderId, status,
                Pagination.normalize(query, SORT_PROPERTIES, DEFAULT_SORT));
    }

    @Transactional
    @Override
    public GoodsReceipt approve(AuthenticatedUser actor, Long id) {
        GoodsReceipt grn = findOrThrow(id);
        // The PO total is the approval basis for the receipt document.
        approvalPolicy.checkCanApprove(actor, grn.getPurchaseOrder().getTotalAmount(),
                "Goods Receipt " + grn.getGrnNumber());
        grn.approve(actor.username(), Instant.now());
        goodsReceiptRepository.save(grn);
        for (GoodsReceiptLine line : grn.getLines()) {
            stockPort.increase(line.getItem(), grn.getWarehouse(), line.getQty(),
                    StockMovementRef.GRN, grn.getId(), grn.getGrnNumber(),
                    "Penerimaan barang", actor.username());
        }
        return grn;
    }

    @Transactional
    @Override
    public GoodsReceipt reject(AuthenticatedUser actor, Long id, String reason) {
        GoodsReceipt grn = findOrThrow(id);
        approvalPolicy.checkCanApprove(actor, grn.getPurchaseOrder().getTotalAmount(),
                "Goods Receipt " + grn.getGrnNumber());
        grn.reject(reason);
        // Rejection never changes stock; the Purchase Order stays APPROVED.
        goodsReceiptRepository.save(grn);
        return grn;
    }

    private GoodsReceipt findOrThrow(Long id) {
        return goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Goods Receipt", id));
    }
}
