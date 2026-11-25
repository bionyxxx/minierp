package com.minierp.infrastructure.persistence.entity;

import java.time.Instant;

import com.minierp.domain.model.shared.DocumentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

/**
 * Shared columns for approval-based documents (PO, GRN, SO, Delivery).
 * Persistence detail of the document aggregates; the pure lifecycle lives in
 * {@code domain.model.shared.BaseApprovalDocument}.
 */
@MappedSuperclass
public abstract class BaseApprovalDocumentJpa {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public DocumentStatus getStatus() {
        return status;
    }

    /** Used by the persistence mapper only. */
    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    /** Used by the persistence mapper only. */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    /** Used by the persistence mapper only. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Used by the persistence mapper only. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    /** Used by the persistence mapper only. */
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    /** Used by the persistence mapper only. */
    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    /** Used by the persistence mapper only. */
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
