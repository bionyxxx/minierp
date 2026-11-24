package com.minierp.purchase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.minierp.common.BaseApprovalDocument;
import com.minierp.warehouse.Warehouse;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "goods_receipts")
public class GoodsReceipt extends BaseApprovalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "goods_receipt_seq")
    @SequenceGenerator(name = "goods_receipt_seq", sequenceName = "goods_receipt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "grn_number", nullable = false, length = 30, unique = true)
    private String grnNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptLine> lines = new ArrayList<>();

    protected GoodsReceipt() {
    }

    public GoodsReceipt(PurchaseOrder purchaseOrder, Warehouse warehouse, LocalDate receiptDate) {
        this.purchaseOrder = purchaseOrder;
        this.warehouse = warehouse;
        this.receiptDate = receiptDate;
    }

    public void addLine(GoodsReceiptLine line) {
        line.setGoodsReceipt(this);
        lines.add(line);
    }

    public Long getId() {
        return id;
    }

    public String getGrnNumber() {
        return grnNumber;
    }

    public void setGrnNumber(String grnNumber) {
        this.grnNumber = grnNumber;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public List<GoodsReceiptLine> getLines() {
        return lines;
    }
}
