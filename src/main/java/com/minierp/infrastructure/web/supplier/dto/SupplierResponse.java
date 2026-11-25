package com.minierp.infrastructure.web.supplier.dto;

import com.minierp.domain.model.supplier.Supplier;

/**
 * External contract of a supplier.
 */
public record SupplierResponse(Long id, String code, String name, String address,
        String phone, String email, boolean active) {

    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getCode(), supplier.getName(),
                supplier.getAddress(), supplier.getPhone(), supplier.getEmail(), supplier.isActive());
    }
}
