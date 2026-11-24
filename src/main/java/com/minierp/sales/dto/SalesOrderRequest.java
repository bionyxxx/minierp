package com.minierp.sales.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a Sales Order.
 */
public record SalesOrderRequest(
        @NotNull(message = "customerId wajib diisi") Long customerId,
        @NotNull(message = "orderDate wajib diisi") LocalDate orderDate,
        String notes,
        @NotEmpty(message = "lines tidak boleh kosong") List<@Valid SalesOrderLineRequest> lines) {
}
