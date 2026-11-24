package com.minierp.sales;

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
@Table(name = "deliveries")
public class Delivery extends BaseApprovalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delivery_seq")
    @SequenceGenerator(name = "delivery_seq", sequenceName = "delivery_seq", allocationSize = 1)
    private Long id;

    @Column(name = "delivery_number", nullable = false, length = 30, unique = true)
    private String deliveryNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryLine> lines = new ArrayList<>();

    protected Delivery() {
    }

    public Delivery(SalesOrder salesOrder, Warehouse warehouse, LocalDate deliveryDate) {
        this.salesOrder = salesOrder;
        this.warehouse = warehouse;
        this.deliveryDate = deliveryDate;
    }

    public void addLine(DeliveryLine line) {
        line.setDelivery(this);
        lines.add(line);
    }

    public Long getId() {
        return id;
    }

    public String getDeliveryNumber() {
        return deliveryNumber;
    }

    public void setDeliveryNumber(String deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public List<DeliveryLine> getLines() {
        return lines;
    }
}
