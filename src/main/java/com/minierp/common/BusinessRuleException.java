package com.minierp.common;

/**
 * A business rule was violated (e.g. stock below order quantity, approver not
 * allowed for the document amount). Mapped to HTTP 422.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
