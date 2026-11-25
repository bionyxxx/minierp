package com.minierp.infrastructure.persistence.repository;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.minierp.domain.model.shared.DocumentNumberType;
import com.minierp.domain.port.out.DocumentNumberPort;

import jakarta.persistence.EntityManager;

/**
 * Persistence adapter — allocates document numbers from the dedicated
 * database sequences and formats them for the external contract. Number
 * formatting stays on {@link DocumentNumberType}.
 */
@Component
public class JpaDocumentNumberRepository implements DocumentNumberPort {

    /** Number sequences are independent from the surrogate id sequences. */
    private static final Map<DocumentNumberType, String> NUMBER_SEQUENCES = Map.of(
            DocumentNumberType.PURCHASE_ORDER, "purchase_order_number_seq",
            DocumentNumberType.GOODS_RECEIPT, "goods_receipt_number_seq",
            DocumentNumberType.SALES_ORDER, "sales_order_number_seq",
            DocumentNumberType.DELIVERY, "delivery_number_seq");

    private final EntityManager entityManager;

    public JpaDocumentNumberRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public String nextNumber(DocumentNumberType type) {
        String sequence = NUMBER_SEQUENCES.get(type);
        Number value = (Number) entityManager
                .createNativeQuery("select nextval('" + sequence + "')")
                .getSingleResult();
        return type.format(value.longValue());
    }
}
