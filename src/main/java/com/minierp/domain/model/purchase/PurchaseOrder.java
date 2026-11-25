package com.minierp.domain.model.purchase;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.minierp.domain.model.shared.BaseApprovalDocument;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.supplier.Supplier;

/**
 * Purchase Order aggregate root. Pure domain model: line handling, totals and
 * the approval lifecycle live here; persistence details stay in the
 * infrastructure layer.
 */
public class PurchaseOrder extends BaseApprovalDocument {

    private Long id;
    private String poNumber;
    private Supplier supplier;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private List<PurchaseOrderLine> lines = new ArrayList<>();

    private PurchaseOrder(Supplier supplier, LocalDate orderDate, String notes, String createdBy) {
        super(notes, createdBy);
        this.supplier = supplier;
        this.orderDate = orderDate;
        this.totalAmount = BigDecimal.ZERO;
    }

    private PurchaseOrder(Supplier supplier, LocalDate orderDate, BigDecimal totalAmount, DocumentStatus status,
            String notes, String createdBy, Instant createdAt, String approvedBy, Instant approvedAt,
            String rejectReason) {
        super(status, notes, createdBy, createdAt, approvedBy, approvedAt, rejectReason);
        this.supplier = supplier;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
    }

    /**
     * Creates a new pending Purchase Order.
     */
    public static PurchaseOrder create(Supplier supplier, LocalDate orderDate, String notes, String createdBy) {
        return new PurchaseOrder(supplier, orderDate, notes, createdBy);
    }

    /**
     * Reconstitutes an existing Purchase Order loaded from persistence.
     */
    public static PurchaseOrder reconstitute(Long id, String poNumber, Supplier supplier, LocalDate orderDate,
            BigDecimal totalAmount, DocumentStatus status, String notes, String createdBy, Instant createdAt,
            String approvedBy, Instant approvedAt, String rejectReason, List<PurchaseOrderLine> lines) {
        PurchaseOrder po = new PurchaseOrder(supplier, orderDate, totalAmount, status, notes, createdBy, createdAt,
                approvedBy, approvedAt, rejectReason);
        po.id = id;
        po.poNumber = poNumber;
        po.lines.addAll(lines);
        return po;
    }

    /**
     * Adds a line and recomputes the document total.
     */
    public void addLine(PurchaseOrderLine line) {
        lines.add(line);
        totalAmount = lines.stream()
                .map(PurchaseOrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Assigns the pre-allocated document number. Must happen before the
     * document is first saved (Hibernate snapshots state at persist time).
     */
    public void assignNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    @Override
    protected String notPendingMessage() {
        return "hanya Purchase Order berstatus PENDING yang dapat diproses";
    }

    public Long getId() {
        return id;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<PurchaseOrderLine> getLines() {
        return lines;
    }
}
