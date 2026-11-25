package com.minierp.domain.port.in;

/**
 * Supplier data for create/update use cases.
 */
public record SupplierCommand(String code, String name, String address, String phone, String email) {
}
