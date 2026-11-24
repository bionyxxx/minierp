package com.minierp.sales;

import java.util.List;

import com.minierp.sales.dto.DeliveryLineResponse;
import com.minierp.sales.dto.DeliveryResponse;

/**
 * Maps {@link Delivery} entities to response DTOs.
 */
final class DeliveryMapper {

    private DeliveryMapper() {
    }

    static DeliveryResponse toResponse(Delivery delivery) {
        List<DeliveryLineResponse> lines = delivery.getLines().stream()
                .map(line -> new DeliveryLineResponse(
                        line.getItem().getId(),
                        line.getItem().getSku(),
                        line.getItem().getName(),
                        line.getQty()))
                .toList();
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
                lines);
    }
}
