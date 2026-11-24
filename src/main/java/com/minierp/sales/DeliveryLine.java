package com.minierp.sales;

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
@Table(name = "delivery_lines")
public class DeliveryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delivery_line_seq")
    @SequenceGenerator(name = "delivery_line_seq", sequenceName = "delivery_line_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal qty;

    protected DeliveryLine() {
    }

    public DeliveryLine(Item item, BigDecimal qty) {
        this.item = item;
        this.qty = qty;
    }

    void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public Long getId() {
        return id;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public Item getItem() {
        return item;
    }

    public BigDecimal getQty() {
        return qty;
    }
}
