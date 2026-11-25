package com.minierp.infrastructure.web.warehouse.dto;

import com.minierp.domain.model.warehouse.Warehouse;

/**
 * External contract of a warehouse.
 */
public record WarehouseResponse(Long id, String code, String name, String location, boolean active) {

    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(warehouse.getId(), warehouse.getCode(), warehouse.getName(),
                warehouse.getLocation(), warehouse.isActive());
    }
}
