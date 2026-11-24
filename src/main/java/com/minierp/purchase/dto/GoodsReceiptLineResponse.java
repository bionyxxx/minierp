package com.minierp.purchase.dto;

import java.math.BigDecimal;

public record GoodsReceiptLineResponse(
        Long itemId,
        String sku,
        String itemName,
        BigDecimal qty) {
}
