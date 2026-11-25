package com.minierp.domain.model.sales;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.minierp.domain.model.shared.BaseApprovalDocument;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.warehouse.Warehouse;

/**
 * Delivery (pengiriman barang) aggregate root, created against an approved
 * Sales Order. Pure domain model.
 */
public class Delivery extends BaseApprovalDocument {

    private Long id;
    private String deliveryNumber;
    private SalesOrder salesOrder;
    private Warehouse warehouse;
    private LocalDate deliveryDate;
    private List<DeliveryLine> lines = new ArrayList<>();

    private Delivery(SalesOrder salesOrder, Warehouse warehouse, LocalDate deliveryDate, String notes,
            String createdBy) {
        super(notes, createdBy);
        this.salesOrder = salesOrder;
        this.warehouse = warehouse;
        this.deliveryDate = deliveryDate;
    }

    private Delivery(SalesOrder salesOrder, Warehouse warehouse, LocalDate deliveryDate, DocumentStatus status,
            String notes, String createdBy, Instant createdAt, String approvedBy, Instant approvedAt,
            String rejectReason) {
        super(status, notes, createdBy, createdAt, approvedBy, approvedAt, rejectReason);
        this.salesOrder = salesOrder;
        this.warehouse = warehouse;
        this.deliveryDate = deliveryDate;
    }

    /**
     * Creates a new pending Delivery.
     */
    public static Delivery create(SalesOrder salesOrder, Warehouse warehouse, LocalDate deliveryDate, String notes,
            String createdBy) {
        return new Delivery(salesOrder, warehouse, deliveryDate, notes, createdBy);
    }

    /**
     * Reconstitutes an existing Delivery loaded from persistence.
     */
    public static Delivery reconstitute(Long id, String deliveryNumber, SalesOrder salesOrder, Warehouse warehouse,
            LocalDate deliveryDate, DocumentStatus status, String notes, String createdBy, Instant createdAt,
            String approvedBy, Instant approvedAt, String rejectReason, List<DeliveryLine> lines) {
        Delivery delivery = new Delivery(salesOrder, warehouse, deliveryDate, status, notes, createdBy, createdAt,
                approvedBy, approvedAt, rejectReason);
        delivery.id = id;
        delivery.deliveryNumber = deliveryNumber;
        delivery.lines.addAll(lines);
        return delivery;
    }

    /**
     * Adds a delivered line.
     */
    public void addLine(DeliveryLine line) {
        lines.add(line);
    }

    /**
     * Rejects a pending Delivery. The reason is mandatory; the pending state is
     * validated first to keep error precedence stable.
     */
    @Override
    public void reject(String reason) {
        requirePending();
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException("reason wajib diisi");
        }
        super.reject(reason);
    }

    /**
     * Assigns the pre-allocated document number. Must happen before the
     * document is first saved (Hibernate snapshots state at persist time).
     */
    public void assignNumber(String deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    @Override
    protected String notPendingMessage() {
        return "Delivery %s hanya dapat disetujui/ditolak saat berstatus PENDING, status saat ini %s"
                .formatted(deliveryNumber, getStatus());
    }

    public Long getId() {
        return id;
    }

    public String getDeliveryNumber() {
        return deliveryNumber;
    }

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public List<DeliveryLine> getLines() {
        return lines;
    }
}
