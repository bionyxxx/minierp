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
 * JPA mapping of the {@code deliveries} table. Persistence detail of the sales
 * aggregate; the domain model lives in {@code domain.model.sales}.
 */
@Entity
@Table(name = "deliveries")
public class DeliveryJpaEntity extends BaseApprovalDocumentJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delivery_seq")
    @SequenceGenerator(name = "delivery_seq", sequenceName = "delivery_seq", allocationSize = 1)
    private Long id;

    @Column(name = "delivery_number", nullable = false, length = 30, unique = true)
    private String deliveryNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrderJpaEntity salesOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseJpaEntity warehouse;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryLineJpaEntity> lines = new ArrayList<>();

    public DeliveryJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    /** Used by the persistence mapper only. */
    public void setId(Long id) {
        this.id = id;
    }

    public String getDeliveryNumber() {
        return deliveryNumber;
    }

    /** Used by the persistence mapper only. */
    public void setDeliveryNumber(String deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    public SalesOrderJpaEntity getSalesOrder() {
        return salesOrder;
    }

    /** Used by the persistence mapper only. */
    public void setSalesOrder(SalesOrderJpaEntity salesOrder) {
        this.salesOrder = salesOrder;
    }

    public WarehouseJpaEntity getWarehouse() {
        return warehouse;
    }

    /** Used by the persistence mapper only. */
    public void setWarehouse(WarehouseJpaEntity warehouse) {
        this.warehouse = warehouse;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    /** Used by the persistence mapper only. */
    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public List<DeliveryLineJpaEntity> getLines() {
        return lines;
    }
}
