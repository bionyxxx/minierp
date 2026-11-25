package com.minierp.domain.port.in;

import java.math.BigDecimal;

/**
 * One line of a create-Purchase-Order command.
 */
public record PurchaseOrderLineCommand(Long itemId, BigDecimal qty, BigDecimal unitPrice) {
}
