package com.minierp.infrastructure.web.sales.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.minierp.domain.model.sales.Delivery;
import com.minierp.domain.model.shared.DocumentStatus;

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

    public static DeliveryResponse from(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getDeliveryNumber(),
                delivery.getSalesOrder().getId(),
                delivery.getSalesOrder().getSoNumber(),
                delivery.getWarehouse().getId(),
                delivery.getWarehouse().getCode(),
                delivery.getDeliveryDate(),
                delivery.getStatus(),
                delivery.getNotes(),
                delivery.getCreatedBy(),
                delivery.getCreatedAt(),
                delivery.getApprovedBy(),
                delivery.getApprovedAt(),
                delivery.getRejectReason(),
                delivery.getLines().stream().map(DeliveryLineResponse::from).toList());
    }
}
