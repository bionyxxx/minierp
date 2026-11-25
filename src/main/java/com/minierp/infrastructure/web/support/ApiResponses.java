package com.minierp.infrastructure.web.support;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ProblemDetail;

import com.minierp.infrastructure.security.CorrelationIdFilter;

/**
 * Builds {@link ApiResponse} envelopes. Shared by the MVC body advice and the
 * security filter-chain handlers so both produce the same shape.
 */
public final class ApiResponses {

    private ApiResponses() {
    }

    public static ApiResponse<?> success(Object body) {
        String correlationId = CorrelationIdFilter.current();
        if (body instanceof PageResponse<?> page) {
            PageMeta pagination = new PageMeta(page.page(), page.size(), page.totalElements(), page.totalPages());
            return ApiResponse.ok(page.content(), ApiMeta.of(correlationId, pagination));
        }
        return ApiResponse.ok(body, ApiMeta.of(correlationId));
    }

    public static ApiResponse<Void> failure(ProblemDetail problem) {
        return ApiResponse.failure(toError(problem), ApiMeta.of(CorrelationIdFilter.current()));
    }

    /**
     * Flattens a problem detail into its RFC 9457 members plus extension
     * properties (e.g. {@code errors}). {@code correlationId} is reported in
     * {@code meta} instead, so it is not duplicated here.
     */
    private static Map<String, Object> toError(ProblemDetail problem) {
        Map<String, Object> error = new LinkedHashMap<>();
        URI type = problem.getType();
        error.put("type", type != null ? type.toString() : null);
        error.put("title", problem.getTitle());
        error.put("status", problem.getStatus());
        error.put("detail", problem.getDetail());
        error.put("instance", problem.getInstance() != null ? problem.getInstance().toString() : null);
        if (problem.getProperties() != null) {
            problem.getProperties().forEach((key, value) -> {
                if (!"correlationId".equals(key)) {
                    error.put(key, value);
                }
            });
        }
        return error;
    }
}
