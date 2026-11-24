package com.minierp.sales.dto;

import java.math.BigDecimal;

/**
 * One line of a Delivery response.
 */
public record DeliveryLineResponse(
        Long itemId,
        String sku,
        String itemName,
        BigDecimal qty) {
}
