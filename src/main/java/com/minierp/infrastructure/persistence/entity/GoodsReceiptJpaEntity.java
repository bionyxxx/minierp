package com.minierp.infrastructure.persistence.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

/**
 * JPA mapping of the {@code goods_receipts} table. Persistence detail of the
 * purchase aggregate; the domain model lives in {@code domain.model.purchase}.
 */
@Entity
@Table(name = "goods_receipts")
public class GoodsReceiptJpaEntity extends BaseApprovalDocumentJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "goods_receipt_seq")
    @SequenceGenerator(name = "goods_receipt_seq", sequenceName = "goods_receipt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "grn_number", nullable = false, length = 30, unique = true)
    private String grnNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrderJpaEntity purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseJpaEntity warehouse;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptLineJpaEntity> lines = new ArrayList<>();

    public GoodsReceiptJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    /** Used by the persistence mapper only. */
    public void setId(Long id) {
        this.id = id;
    }

    public String getGrnNumber() {
        return grnNumber;
    }

    /** Used by the persistence mapper only. */
    public void setGrnNumber(String grnNumber) {
        this.grnNumber = grnNumber;
    }

    public PurchaseOrderJpaEntity getPurchaseOrder() {
        return purchaseOrder;
    }

    /** Used by the persistence mapper only. */
    public void setPurchaseOrder(PurchaseOrderJpaEntity purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public WarehouseJpaEntity getWarehouse() {
        return warehouse;
    }

    /** Used by the persistence mapper only. */
    public void setWarehouse(WarehouseJpaEntity warehouse) {
        this.warehouse = warehouse;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    /** Used by the persistence mapper only. */
    public void setReceiptDate(LocalDate receiptDate) {
        this.receiptDate = receiptDate;
    }

    public List<GoodsReceiptLineJpaEntity> getLines() {
        return lines;
    }
}
