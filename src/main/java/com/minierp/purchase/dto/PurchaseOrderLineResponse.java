package com.minierp.purchase.dto;

import java.math.BigDecimal;

public record PurchaseOrderLineResponse(
        Long itemId,
        String sku,
        String itemName,
        BigDecimal qty,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
