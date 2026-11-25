package com.minierp.infrastructure.web.warehouse.dto;

import com.minierp.domain.port.in.WarehouseCommand;

import jakarta.validation.constraints.NotBlank;

/**
 * Create/update payload for a warehouse.
 */
public record WarehouseRequest(
        @NotBlank(message = "kode wajib diisi") String code,
        @NotBlank(message = "nama wajib diisi") String name,
        String location) {

    public WarehouseCommand toCommand() {
        return new WarehouseCommand(code, name, location);
    }
}
