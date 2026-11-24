package com.minierp.item.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

/**
 * Create/update payload for an item. Nullable numeric fields default to zero.
 */
public record ItemRequest(
        @NotBlank(message = "sku wajib diisi") String sku,
        @NotBlank(message = "nama wajib diisi") String name,
        @NotBlank(message = "satuan wajib diisi") String uom,
        @DecimalMin(value = "0", message = "stok minimum tidak boleh kurang dari 0") BigDecimal minStock,
        @DecimalMin(value = "0", message = "harga beli tidak boleh kurang dari 0") BigDecimal purchasePrice,
        @DecimalMin(value = "0", message = "harga jual tidak boleh kurang dari 0") BigDecimal sellingPrice) {
}
