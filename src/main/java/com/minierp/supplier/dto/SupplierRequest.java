package com.minierp.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Create/update payload for a supplier.
 */
public record SupplierRequest(
        @NotBlank(message = "kode wajib diisi") String code,
        @NotBlank(message = "nama wajib diisi") String name,
        String address,
        String phone,
        @Email(message = "format email tidak valid") String email) {
}
