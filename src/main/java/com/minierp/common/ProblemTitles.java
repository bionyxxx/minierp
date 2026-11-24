package com.minierp.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Stable titles for RFC 9457 error responses.
 */
public final class ProblemTitles {

    public static final String NOT_FOUND = "Resource tidak ditemukan";
    public static final String BUSINESS_RULE_VIOLATION = "Aturan bisnis dilanggar";
    public static final String INVALID_STATE = "Status dokumen tidak memungkinkan";
    public static final String DUPLICATE_RESOURCE = "Data sudah ada";
    public static final String VALIDATION_FAILED = "Validasi request gagal";
    public static final String PARAMETER_MISMATCH = "Parameter request tidak valid";
    public static final String INVALID_SORT = "Parameter sort tidak valid";
    public static final String INVALID_API_VERSION = "Versi API tidak valid";
    public static final String MALFORMED_REQUEST = "Format request tidak valid";
    public static final String METHOD_NOT_ALLOWED = "Method tidak diizinkan";
    public static final String BAD_REQUEST = "Permintaan tidak valid";
    public static final String NOT_ACCEPTABLE = "Format response tidak didukung";
    public static final String UNSUPPORTED_MEDIA_TYPE = "Format request tidak didukung";
    public static final String UNAUTHORIZED = "Autentikasi diperlukan";
    public static final String FORBIDDEN = "Akses ditolak";
    public static final String LOGIN_FAILED = "Login gagal";
    public static final String INTERNAL_ERROR = "Terjadi kesalahan pada server";

    private ProblemTitles() {
    }

    /**
     * Title for framework exceptions without a dedicated problem type.
     */
    public static String forStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> BAD_REQUEST;
            case 404 -> NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 406 -> NOT_ACCEPTABLE;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            case 500 -> INTERNAL_ERROR;
            default -> status instanceof HttpStatus httpStatus
                    ? httpStatus.getReasonPhrase()
                    : "HTTP " + status.value();
        };
    }
}
