package com.minierp.warehouse.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Create/update payload for a warehouse.
 */
public record WarehouseRequest(
        @NotBlank(message = "kode wajib diisi") String code,
        @NotBlank(message = "nama wajib diisi") String name,
        String location) {
}
