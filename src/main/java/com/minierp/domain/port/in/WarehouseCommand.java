package com.minierp.domain.port.in;

/**
 * Warehouse data for create/update use cases.
 */
public record WarehouseCommand(String code, String name, String location) {
}
