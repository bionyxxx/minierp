package com.minierp.sales.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a Delivery against an approved Sales Order.
 */
public record DeliveryRequest(
        @NotNull(message = "salesOrderId wajib diisi") Long salesOrderId,
        @NotNull(message = "warehouseId wajib diisi") Long warehouseId,
        @NotNull(message = "deliveryDate wajib diisi") LocalDate deliveryDate,
        String notes,
        @NotEmpty(message = "lines tidak boleh kosong") List<@Valid DeliveryLineRequest> lines) {
}
