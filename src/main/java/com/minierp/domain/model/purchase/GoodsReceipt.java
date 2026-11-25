package com.minierp.domain.model.purchase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.minierp.domain.model.shared.BaseApprovalDocument;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.warehouse.Warehouse;

/**
 * Goods Receipt (penerimaan barang) aggregate root, created against an
 * approved Purchase Order. Pure domain model.
 */
public class GoodsReceipt extends BaseApprovalDocument {

    private Long id;
    private String grnNumber;
    private PurchaseOrder purchaseOrder;
    private Warehouse warehouse;
    private LocalDate receiptDate;
    private List<GoodsReceiptLine> lines = new ArrayList<>();

    private GoodsReceipt(PurchaseOrder purchaseOrder, Warehouse warehouse, LocalDate receiptDate, String notes,
            String createdBy) {
        super(notes, createdBy);
        this.purchaseOrder = purchaseOrder;
        this.warehouse = warehouse;
        this.receiptDate = receiptDate;
    }

    private GoodsReceipt(PurchaseOrder purchaseOrder, Warehouse warehouse, LocalDate receiptDate, DocumentStatus status,
            String notes, String createdBy, Instant createdAt, String approvedBy, Instant approvedAt,
            String rejectReason) {
        super(status, notes, createdBy, createdAt, approvedBy, approvedAt, rejectReason);
        this.purchaseOrder = purchaseOrder;
        this.warehouse = warehouse;
        this.receiptDate = receiptDate;
    }

    /**
     * Creates a new pending Goods Receipt.
     */
    public static GoodsReceipt create(PurchaseOrder purchaseOrder, Warehouse warehouse, LocalDate receiptDate,
            String notes, String createdBy) {
        return new GoodsReceipt(purchaseOrder, warehouse, receiptDate, notes, createdBy);
    }

    /**
     * Reconstitutes an existing Goods Receipt loaded from persistence.
     */
    public static GoodsReceipt reconstitute(Long id, String grnNumber, PurchaseOrder purchaseOrder, Warehouse warehouse,
            LocalDate receiptDate, DocumentStatus status, String notes, String createdBy, Instant createdAt,
            String approvedBy, Instant approvedAt, String rejectReason, List<GoodsReceiptLine> lines) {
        GoodsReceipt grn = new GoodsReceipt(purchaseOrder, warehouse, receiptDate, status, notes, createdBy, createdAt,
                approvedBy, approvedAt, rejectReason);
        grn.id = id;
        grn.grnNumber = grnNumber;
        grn.lines.addAll(lines);
        return grn;
    }

    /**
     * Adds a received line.
     */
    public void addLine(GoodsReceiptLine line) {
        lines.add(line);
    }

    /**
     * Assigns the pre-allocated document number. Must happen before the
     * document is first saved (Hibernate snapshots state at persist time).
     */
    public void assignNumber(String grnNumber) {
        this.grnNumber = grnNumber;
    }

    @Override
    protected String notPendingMessage() {
        return "hanya Goods Receipt berstatus PENDING yang dapat diproses";
    }

    public Long getId() {
        return id;
    }

    public String getGrnNumber() {
        return grnNumber;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public List<GoodsReceiptLine> getLines() {
        return lines;
    }
}
