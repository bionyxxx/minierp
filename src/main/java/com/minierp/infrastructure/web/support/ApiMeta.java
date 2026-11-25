package com.minierp.infrastructure.web.support;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Response metadata. {@code pagination} is present only on paginated list
 * endpoints.
 */
@JsonPropertyOrder({"timestamp", "correlationId", "pagination"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiMeta(Instant timestamp, String correlationId, PageMeta pagination) {

    public static ApiMeta of(String correlationId) {
        return new ApiMeta(Instant.now(), correlationId, null);
    }

    public static ApiMeta of(String correlationId, PageMeta pagination) {
        return new ApiMeta(Instant.now(), correlationId, pagination);
    }
}
