package com.minierp.infrastructure.persistence.entity;

import java.math.BigDecimal;
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
 * JPA mapping of the {@code sales_orders} table. Persistence detail of the
 * sales aggregate; the domain model lives in {@code domain.model.sales}.
 */
@Entity
@Table(name = "sales_orders")
public class SalesOrderJpaEntity extends BaseApprovalDocumentJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_order_seq")
    @SequenceGenerator(name = "sales_order_seq", sequenceName = "sales_order_seq", allocationSize = 1)
    private Long id;

    @Column(name = "so_number", nullable = false, length = 30, unique = true)
    private String soNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesOrderLineJpaEntity> lines = new ArrayList<>();

    public SalesOrderJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    /** Used by the persistence mapper only. */
    public void setId(Long id) {
        this.id = id;
    }

    public String getSoNumber() {
        return soNumber;
    }

    /** Used by the persistence mapper only. */
    public void setSoNumber(String soNumber) {
        this.soNumber = soNumber;
    }

    public CustomerJpaEntity getCustomer() {
        return customer;
    }

    /** Used by the persistence mapper only. */
    public void setCustomer(CustomerJpaEntity customer) {
        this.customer = customer;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    /** Used by the persistence mapper only. */
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /** Used by the persistence mapper only. */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<SalesOrderLineJpaEntity> getLines() {
        return lines;
    }
}
