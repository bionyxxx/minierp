package com.minierp.infrastructure.web.sales.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for rejecting a document. The reason is mandatory.
 */
public record RejectRequest(
        @NotBlank(message = "reason wajib diisi") String reason) {
}
