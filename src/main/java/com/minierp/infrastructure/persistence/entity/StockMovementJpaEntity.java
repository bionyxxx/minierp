package com.minierp.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.minierp.domain.model.inventory.StockMovementRef;
import com.minierp.domain.model.inventory.StockMovementType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * JPA mapping of the {@code stock_movements} table. Persistence detail of the
 * inventory aggregate; the domain model lives in {@code domain.model.inventory}.
 */
@Entity
@Table(name = "stock_movements")
public class StockMovementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemJpaEntity item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseJpaEntity warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 10)
    private StockMovementType movementType;

    @Column(name = "qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal qty;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false, length = 20)
    private StockMovementRef refType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "ref_number", nullable = false, length = 30)
    private String refNumber;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public StockMovementJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    /** Used by the persistence mapper only. */
    public void setId(Long id) {
        this.id = id;
    }

    public ItemJpaEntity getItem() {
        return item;
    }

    /** Used by the persistence mapper only. */
    public void setItem(ItemJpaEntity item) {
        this.item = item;
    }

    public WarehouseJpaEntity getWarehouse() {
        return warehouse;
    }

    /** Used by the persistence mapper only. */
    public void setWarehouse(WarehouseJpaEntity warehouse) {
        this.warehouse = warehouse;
    }

    public StockMovementType getMovementType() {
        return movementType;
    }

    /** Used by the persistence mapper only. */
    public void setMovementType(StockMovementType movementType) {
        this.movementType = movementType;
    }

    public BigDecimal getQty() {
        return qty;
    }

    /** Used by the persistence mapper only. */
    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public StockMovementRef getRefType() {
        return refType;
    }

    /** Used by the persistence mapper only. */
    public void setRefType(StockMovementRef refType) {
        this.refType = refType;
    }

    public Long getRefId() {
        return refId;
    }

    /** Used by the persistence mapper only. */
    public void setRefId(Long refId) {
        this.refId = refId;
    }

    public String getRefNumber() {
        return refNumber;
    }

    /** Used by the persistence mapper only. */
    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }

    public String getRemark() {
        return remark;
    }

    /** Used by the persistence mapper only. */
    public void setRemark(String remark) {
        this.remark = remark;
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
}
