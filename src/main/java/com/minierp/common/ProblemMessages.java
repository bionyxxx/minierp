package com.minierp.common;

/**
 * Fixed detail messages for RFC 9457 error responses. Dynamic details (e.g.
 * business rule messages) still come from the exception itself.
 */
public final class ProblemMessages {

    public static final String UNAUTHORIZED = "Token tidak valid, kedaluwarsa, atau tidak disertakan";
    public static final String FORBIDDEN = "Anda tidak memiliki hak untuk melakukan aksi ini";
    public static final String LOGIN_FAILED = "Username atau password salah";
    public static final String MALFORMED_REQUEST = "Format body request tidak valid";
    public static final String INTERNAL_ERROR =
            "Terjadi kesalahan tak terduga. Silakan coba lagi atau hubungi administrator.";

    /** Format for an unsupported API version; argument is the requested version. */
    public static final String INVALID_API_VERSION_FORMAT = "Versi API '%s' tidak valid atau tidak didukung";

    /** Detail for a request without a required API version. */
    public static final String MISSING_API_VERSION = "Versi API wajib disertakan";

    /** Format for validation failure detail; argument is the number of invalid fields. */
    public static final String VALIDATION_FAILED_FORMAT = "%d field tidak valid";

    /** Format for an invalid sort property; arguments are the property and the allowlist. */
    public static final String INVALID_SORT_FORMAT = "Properti sort '%s' tidak dikenali. Properti yang diizinkan: %s";

    private ProblemMessages() {
    }
}
