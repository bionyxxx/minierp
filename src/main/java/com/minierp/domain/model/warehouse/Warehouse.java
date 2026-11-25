package com.minierp.domain.model.warehouse;

import java.time.Instant;

/**
 * Warehouse master data. Pure domain model.
 */
public class Warehouse {

    private Long id;
    private String code;
    private String name;
    private String location;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private Warehouse() {
    }

    public static Warehouse create(String code, String name, String location) {
        Warehouse warehouse = new Warehouse();
        warehouse.code = code;
        warehouse.name = name;
        warehouse.location = location;
        warehouse.active = true;
        return warehouse;
    }

    /**
     * Reconstitutes an existing warehouse loaded from persistence.
     */
    public static Warehouse reconstitute(Long id, String code, String name, String location,
            boolean active, Instant createdAt, Instant updatedAt) {
        Warehouse warehouse = new Warehouse();
        warehouse.id = id;
        warehouse.code = code;
        warehouse.name = name;
        warehouse.location = location;
        warehouse.active = active;
        warehouse.createdAt = createdAt;
        warehouse.updatedAt = updatedAt;
        return warehouse;
    }

    public void update(String code, String name, String location) {
        this.code = code;
        this.name = name;
        this.location = location;
    }

    /**
     * Soft delete: the warehouse stays referenced by stock/documents but is deactivated.
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

    public String getLocation() {
        return location;
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
