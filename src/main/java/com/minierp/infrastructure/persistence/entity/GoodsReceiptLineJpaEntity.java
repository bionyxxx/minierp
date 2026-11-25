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
 * JPA mapping of the {@code goods_receipt_lines} table. Persistence detail of
 * the purchase aggregate; the domain model lives in
 * {@code domain.model.purchase}.
 */
@Entity
@Table(name = "goods_receipt_lines")
public class GoodsReceiptLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "goods_receipt_line_seq")
    @SequenceGenerator(name = "goods_receipt_line_seq", sequenceName = "goods_receipt_line_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceiptJpaEntity goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemJpaEntity item;

    @Column(name = "qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal qty;

    public GoodsReceiptLineJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    /** Used by the persistence mapper only. */
    public void setId(Long id) {
        this.id = id;
    }

    public GoodsReceiptJpaEntity getGoodsReceipt() {
        return goodsReceipt;
    }

    /** Used by the persistence mapper only. */
    public void setGoodsReceipt(GoodsReceiptJpaEntity goodsReceipt) {
        this.goodsReceipt = goodsReceipt;
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
}
