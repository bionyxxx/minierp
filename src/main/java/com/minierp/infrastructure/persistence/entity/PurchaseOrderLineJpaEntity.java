package com.minierp.infrastructure.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * JPA mapping of the {@code purchase_order_lines} table. Persistence detail of
 * the purchase aggregate; the domain model lives in
 * {@code domain.model.purchase}.
 */
@Entity
@Table(name = "purchase_order_lines")
public class PurchaseOrderLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchase_order_line_seq")
    @SequenceGenerator(name = "purchase_order_line_seq", sequenceName = "purchase_order_line_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrderJpaEntity purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemJpaEntity item;

    @Column(name = "qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal qty;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineTotal;

    public PurchaseOrderLineJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    /** Used by the persistence mapper only. */
    public void setId(Long id) {
        this.id = id;
    }

    public PurchaseOrderJpaEntity getPurchaseOrder() {
        return purchaseOrder;
    }

    /** Used by the persistence mapper only. */
    public void setPurchaseOrder(PurchaseOrderJpaEntity purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public ItemJpaEntity getItem() {
        return item;
    }

    /** Used by the persistence mapper only. */
    public void setItem(ItemJpaEntity item) {
        this.item = item;
    }

    public BigDecimal getQty() {
        return qty;
    }

    /** Used by the persistence mapper only. */
    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /** Used by the persistence mapper only. */
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    /** Used by the persistence mapper only. */
    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
