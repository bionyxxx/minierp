package com.minierp.purchase;

import java.math.BigDecimal;

import com.minierp.item.Item;

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

@Entity
@Table(name = "goods_receipt_lines")
public class GoodsReceiptLine {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "goods_receipt_line_seq")
    @SequenceGenerator(name = "goods_receipt_line_seq", sequenceName = "goods_receipt_line_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal qty;

    protected GoodsReceiptLine() {
    }

    public GoodsReceiptLine(Item item, BigDecimal qty) {
        this.item = item;
        this.qty = qty;
    }

    void setGoodsReceipt(GoodsReceipt goodsReceipt) {
        this.goodsReceipt = goodsReceipt;
    }

    public Long getId() {
        return id;
    }

    public GoodsReceipt getGoodsReceipt() {
        return goodsReceipt;
    }

    public Item getItem() {
        return item;
    }

    public BigDecimal getQty() {
        return qty;
    }
}
