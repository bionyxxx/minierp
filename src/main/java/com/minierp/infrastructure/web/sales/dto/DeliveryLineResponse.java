package com.minierp.infrastructure.web.sales.dto;

import java.math.BigDecimal;

import com.minierp.domain.model.sales.DeliveryLine;

/**
 * One line of a Delivery response.
 */
public record DeliveryLineResponse(
        Long itemId,
        String sku,
        String itemName,
        BigDecimal qty) {

    public static DeliveryLineResponse from(DeliveryLine line) {
        return new DeliveryLineResponse(line.getItem().getId(), line.getItem().getSku(),
                line.getItem().getName(), line.getQty());
    }
}
