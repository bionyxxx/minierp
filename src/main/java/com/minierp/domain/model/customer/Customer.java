package com.minierp.domain.model.customer;

import java.time.Instant;

/**
 * Customer master data. Pure domain model.
 */
public class Customer {

    private Long id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private String email;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private Customer() {
    }

    public static Customer create(String code, String name, String address, String phone, String email) {
        Customer customer = new Customer();
        customer.code = code;
        customer.name = name;
        customer.address = address;
        customer.phone = phone;
        customer.email = email;
        customer.active = true;
        return customer;
    }

    /**
     * Reconstitutes an existing customer loaded from persistence.
     */
    public static Customer reconstitute(Long id, String code, String name, String address, String phone,
            String email, boolean active, Instant createdAt, Instant updatedAt) {
        Customer customer = new Customer();
        customer.id = id;
        customer.code = code;
        customer.name = name;
        customer.address = address;
        customer.phone = phone;
        customer.email = email;
        customer.active = active;
        customer.createdAt = createdAt;
        customer.updatedAt = updatedAt;
        return customer;
    }

    public void update(String code, String name, String address, String phone, String email) {
        this.code = code;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Soft delete: the customer stays referenced by documents but is deactivated.
     */
    public void deactivate() {
        this.active = false;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
