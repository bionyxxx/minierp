package com.minierp.sales.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * One line of a create-Sales-Order request.
 */
public record SalesOrderLineRequest(
        @NotNull(message = "itemId wajib diisi") Long itemId,
        @NotNull(message = "qty wajib diisi")
        @DecimalMin(value = "0.001", message = "qty harus lebih besar dari 0") BigDecimal qty,
        @NotNull(message = "unitPrice wajib diisi")
        @DecimalMin(value = "0", message = "unitPrice tidak boleh negatif") BigDecimal unitPrice) {
}
