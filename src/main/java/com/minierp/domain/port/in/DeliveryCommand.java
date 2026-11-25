package com.minierp.domain.port.in;

import java.time.LocalDate;
import java.util.List;

/**
 * Delivery data for the create use case.
 */
public record DeliveryCommand(Long salesOrderId, Long warehouseId, LocalDate deliveryDate, String notes,
        List<DeliveryLineCommand> lines) {

    public DeliveryCommand {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }
}
