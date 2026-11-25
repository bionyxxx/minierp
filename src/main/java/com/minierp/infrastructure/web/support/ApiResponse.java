package com.minierp.infrastructure.web.support;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Uniform envelope for every API response body:
 * {@code {"success": ..., "data": ..., "meta": ..., "error": ...}}.
 * <p>
 * {@code data} carries the payload on success and is {@code null} on failure;
 * {@code error} carries the RFC 9457 problem fields on failure and is omitted
 * on success. Controllers return plain DTOs — {@link ApiResponseBodyAdvice}
 * wraps them.
 */
@JsonPropertyOrder({"success", "data", "meta", "error"})
public record ApiResponse<T>(
        boolean success,
        @JsonInclude(JsonInclude.Include.ALWAYS) T data,
        ApiMeta meta,
        @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, Object> error) {

    public static <T> ApiResponse<T> ok(T data, ApiMeta meta) {
        return new ApiResponse<>(true, data, meta, null);
    }

    public static ApiResponse<Void> failure(Map<String, Object> error, ApiMeta meta) {
        return new ApiResponse<>(false, null, meta, error);
    }
}
