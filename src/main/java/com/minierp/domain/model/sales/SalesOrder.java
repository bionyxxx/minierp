package com.minierp.domain.model.sales;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.minierp.domain.model.customer.Customer;
import com.minierp.domain.model.shared.BaseApprovalDocument;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.shared.DocumentStatus;

/**
 * Sales Order aggregate root. Pure domain model: line handling, totals and the
 * approval lifecycle live here; persistence details stay in the
 * infrastructure layer.
 */
public class SalesOrder extends BaseApprovalDocument {

    private Long id;
    private String soNumber;
    private Customer customer;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private List<SalesOrderLine> lines = new ArrayList<>();

    private SalesOrder(Customer customer, LocalDate orderDate, String notes, String createdBy) {
        super(notes, createdBy);
        this.customer = customer;
        this.orderDate = orderDate;
        this.totalAmount = BigDecimal.ZERO;
    }

    private SalesOrder(Customer customer, LocalDate orderDate, BigDecimal totalAmount, DocumentStatus status,
            String notes, String createdBy, Instant createdAt, String approvedBy, Instant approvedAt,
            String rejectReason) {
        super(status, notes, createdBy, createdAt, approvedBy, approvedAt, rejectReason);
        this.customer = customer;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
    }

    /**
     * Creates a new pending Sales Order.
     */
    public static SalesOrder create(Customer customer, LocalDate orderDate, String notes, String createdBy) {
        return new SalesOrder(customer, orderDate, notes, createdBy);
    }

    /**
     * Reconstitutes an existing Sales Order loaded from persistence.
     */
    public static SalesOrder reconstitute(Long id, String soNumber, Customer customer, LocalDate orderDate,
            BigDecimal totalAmount, DocumentStatus status, String notes, String createdBy, Instant createdAt,
            String approvedBy, Instant approvedAt, String rejectReason, List<SalesOrderLine> lines) {
        SalesOrder so = new SalesOrder(customer, orderDate, totalAmount, status, notes, createdBy, createdAt,
                approvedBy, approvedAt, rejectReason);
        so.id = id;
        so.soNumber = soNumber;
        so.lines.addAll(lines);
        return so;
    }

    /**
     * Adds a line and recomputes the document total.
     */
    public void addLine(SalesOrderLine line) {
        lines.add(line);
        totalAmount = lines.stream()
                .map(SalesOrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Rejects a pending Sales Order. The reason is mandatory; the pending state
     * is validated first to keep error precedence stable.
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
    public void assignNumber(String soNumber) {
        this.soNumber = soNumber;
    }

    @Override
    protected String notPendingMessage() {
        return "Sales Order %s hanya dapat disetujui/ditolak saat berstatus PENDING, status saat ini %s"
                .formatted(soNumber, getStatus());
    }

    public Long getId() {
        return id;
    }

    public String getSoNumber() {
        return soNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<SalesOrderLine> getLines() {
        return lines;
    }
}
