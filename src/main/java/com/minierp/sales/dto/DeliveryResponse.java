package com.minierp.sales.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.minierp.common.DocumentStatus;

/**
 * Delivery response, including its lines.
 */
public record DeliveryResponse(
        Long id,
        String deliveryNumber,
        Long salesOrderId,
        String soNumber,
        Long warehouseId,
        String warehouseCode,
        LocalDate deliveryDate,
        DocumentStatus status,
        String notes,
        String createdBy,
        Instant createdAt,
        String approvedBy,
        Instant approvedAt,
        String rejectReason,
        List<DeliveryLineResponse> lines) {
}
