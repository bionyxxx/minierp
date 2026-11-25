package com.minierp.domain.port.out;

import com.minierp.domain.model.shared.DocumentNumberType;

/**
 * Driven port: document number allocation. Numbers come from dedicated
 * database sequences and must be assigned before a document is first saved.
 */
public interface DocumentNumberPort {

    /**
     * Pre-allocates the next external document number for the given kind,
     * e.g. {@code PO-000123}.
     */
    String nextNumber(DocumentNumberType type);
}
