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
 * JPA mapping of the {@code sales_order_lines} table. Persistence detail of the
 * sales aggregate; the domain model lives in {@code domain.model.sales}.
 */
@Entity
@Table(name = "sales_order_lines")
public class SalesOrderLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_order_line_seq")
    @SequenceGenerator(name = "sales_order_line_seq", sequenceName = "sales_order_line_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrderJpaEntity salesOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemJpaEntity item;

    @Column(name = "qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal qty;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineTotal;

    public SalesOrderLineJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    /** Used by the persistence mapper only. */
    public void setId(Long id) {
        this.id = id;
    }

    public SalesOrderJpaEntity getSalesOrder() {
        return salesOrder;
    }

    /** Used by the persistence mapper only. */
    public void setSalesOrder(SalesOrderJpaEntity salesOrder) {
        this.salesOrder = salesOrder;
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
