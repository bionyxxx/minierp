package com.minierp.domain.model.supplier;

import java.time.Instant;

/**
 * Supplier master data. Pure domain model.
 */
public class Supplier {

    private Long id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private String email;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private Supplier() {
    }

    public static Supplier create(String code, String name, String address, String phone, String email) {
        Supplier supplier = new Supplier();
        supplier.code = code;
        supplier.name = name;
        supplier.address = address;
        supplier.phone = phone;
        supplier.email = email;
        supplier.active = true;
        return supplier;
    }

    /**
     * Reconstitutes an existing supplier loaded from persistence.
     */
    public static Supplier reconstitute(Long id, String code, String name, String address, String phone,
            String email, boolean active, Instant createdAt, Instant updatedAt) {
        Supplier supplier = new Supplier();
        supplier.id = id;
        supplier.code = code;
        supplier.name = name;
        supplier.address = address;
        supplier.phone = phone;
        supplier.email = email;
        supplier.active = active;
        supplier.createdAt = createdAt;
        supplier.updatedAt = updatedAt;
        return supplier;
    }

    public void update(String code, String name, String address, String phone, String email) {
        this.code = code;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Soft delete: the supplier stays referenced by documents but is deactivated.
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
