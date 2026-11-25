package com.minierp.domain.model.shared;

import java.time.Instant;

/**
 * Shared lifecycle of approval-based documents (PO, GRN, SO, Delivery).
 * Pure domain model: status transitions and their guards live here;
 * persistence details stay in the infrastructure layer.
 */
public abstract class BaseApprovalDocument {

    private DocumentStatus status;
    private String notes;
    private String createdBy;
    private Instant createdAt;
    private String approvedBy;
    private Instant approvedAt;
    private String rejectReason;

    protected BaseApprovalDocument(String notes, String createdBy) {
        this.status = DocumentStatus.PENDING;
        this.notes = notes;
        this.createdBy = createdBy;
    }

    protected BaseApprovalDocument(DocumentStatus status, String notes, String createdBy, Instant createdAt,
            String approvedBy, Instant approvedAt, String rejectReason) {
        this.status = status;
        this.notes = notes;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectReason = rejectReason;
    }

    /**
     * Approves a pending document and records the decision.
     *
     * @throws InvalidStateException when the document has already been decided
     */
    public void approve(String actor, Instant approvedAt) {
        requirePending();
        this.status = DocumentStatus.APPROVED;
        this.approvedBy = actor;
        this.approvedAt = approvedAt;
    }

    /**
     * Rejects a pending document and records the reason.
     *
     * @throws InvalidStateException when the document has already been decided
     */
    public void reject(String reason) {
        requirePending();
        this.status = DocumentStatus.REJECTED;
        this.rejectReason = reason;
    }

    protected void requirePending() {
        if (status != DocumentStatus.PENDING) {
            throw new InvalidStateException(notPendingMessage());
        }
    }

    /**
     * Error message used when a decision is attempted on a document that is no
     * longer pending. The wording differs per document type.
     */
    protected abstract String notPendingMessage();

    public DocumentStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }
}
