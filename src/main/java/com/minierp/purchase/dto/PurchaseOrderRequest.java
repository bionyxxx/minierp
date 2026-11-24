package com.minierp.purchase.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PurchaseOrderRequest(
        @NotNull(message = "supplierId wajib diisi") Long supplierId,
        @NotNull(message = "orderDate wajib diisi") LocalDate orderDate,
        String notes,
        @NotEmpty(message = "Purchase Order minimal memiliki 1 line")
        List<@Valid PurchaseOrderLineRequest> lines) {
}
