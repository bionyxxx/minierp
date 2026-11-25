package com.minierp.infrastructure.web.error;

/**
 * Stable problem type slugs for RFC 9457 error responses. The type URI of
 * every error resolves against {@code minierp.api.problem-base-uri}; see
 * {@link ProblemTypeUris} and the problem catalog at {@code GET /api/problems}.
 */
public final class ProblemTypes {

    public static final String NOT_FOUND = "not-found";
    public static final String BUSINESS_RULE_VIOLATION = "business-rule-violation";
    public static final String INVALID_STATE = "invalid-state";
    public static final String DUPLICATE_RESOURCE = "duplicate-resource";
    public static final String VALIDATION_FAILED = "validation-failed";
    public static final String PARAMETER_MISMATCH = "parameter-mismatch";
    public static final String INVALID_SORT = "invalid-sort";
    public static final String INVALID_API_VERSION = "invalid-api-version";
    public static final String MALFORMED_REQUEST = "malformed-request";
    public static final String METHOD_NOT_ALLOWED = "method-not-allowed";
    public static final String BAD_REQUEST = "bad-request";
    public static final String NOT_ACCEPTABLE = "not-acceptable";
    public static final String UNSUPPORTED_MEDIA_TYPE = "unsupported-media-type";
    public static final String UNAUTHORIZED = "unauthorized";
    public static final String FORBIDDEN = "forbidden";
    public static final String LOGIN_FAILED = "login-failed";
    public static final String INTERNAL_ERROR = "internal-error";

    private ProblemTypes() {
    }

    /**
     * Generic slug for framework exceptions without a dedicated problem type.
     */
    public static String forStatus(int statusCode) {
        return switch (statusCode) {
            case 400 -> BAD_REQUEST;
            case 404 -> NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 406 -> NOT_ACCEPTABLE;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            case 500 -> INTERNAL_ERROR;
            default -> "http-" + statusCode;
        };
    }
}
