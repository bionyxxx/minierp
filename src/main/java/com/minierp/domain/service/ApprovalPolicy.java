package com.minierp.domain.service;

import java.math.BigDecimal;

import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.BusinessRuleException;

/**
 * Approval authority rules. Pure domain service; the threshold is injected so
 * the domain stays free of configuration concerns.
 *
 * <ul>
 *   <li>Documents with total &gt;= threshold (10.000.000) must be approved by
 *       ADMIN or MANAGER.</li>
 *   <li>Documents below the threshold may also be approved by STAFF.</li>
 * </ul>
 */
public class ApprovalPolicy {

    private final BigDecimal threshold;

    public ApprovalPolicy(BigDecimal threshold) {
        this.threshold = threshold;
    }

    /**
     * @throws BusinessRuleException when the user may not decide (approve/reject)
     *                               a document of the given amount
     */
    public void checkCanApprove(AuthenticatedUser user, BigDecimal documentAmount, String documentLabel) {
        if (documentAmount == null) {
            throw new IllegalArgumentException("documentAmount tidak boleh null");
        }
        boolean aboveThreshold = documentAmount.compareTo(threshold) >= 0;
        if (aboveThreshold && user.isStaff()) {
            throw new BusinessRuleException(
                    "%s dengan nilai %s (>= %s) hanya boleh disetujui/ditolak oleh Admin atau Manager"
                            .formatted(documentLabel, documentAmount.toPlainString(), threshold.toPlainString()));
        }
    }

    public BigDecimal threshold() {
        return threshold;
    }
}
