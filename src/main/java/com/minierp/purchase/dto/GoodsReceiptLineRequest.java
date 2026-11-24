package com.minierp.purchase.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record GoodsReceiptLineRequest(
        @NotNull(message = "itemId wajib diisi") Long itemId,
        @NotNull(message = "qty wajib diisi")
        @DecimalMin(value = "0.001", message = "qty harus lebih dari 0") BigDecimal qty) {
}
