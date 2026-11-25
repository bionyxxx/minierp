package com.minierp.infrastructure.web.purchase.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(
        @NotBlank(message = "reason wajib diisi") String reason) {
}
