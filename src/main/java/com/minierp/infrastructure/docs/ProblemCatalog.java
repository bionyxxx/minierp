package com.minierp.infrastructure.docs;

import java.util.List;
import java.util.Optional;

import com.minierp.infrastructure.web.error.ProblemTitles;
import com.minierp.infrastructure.web.error.ProblemTypes;

/**
 * Registry of every documented problem type. Single source of truth for the
 * problem catalog endpoint and for the RFC 9457 {@code type} URIs.
 */
public final class ProblemCatalog {

    private static final List<ProblemDescriptor> DESCRIPTORS = List.of(
            new ProblemDescriptor(ProblemTypes.NOT_FOUND, ProblemTitles.NOT_FOUND, 404,
                    "Resource (item, dokumen, ...) dengan id yang diminta tidak ada."),
            new ProblemDescriptor(ProblemTypes.BUSINESS_RULE_VIOLATION, ProblemTitles.BUSINESS_RULE_VIOLATION, 422,
                    "Permintaan melanggar aturan bisnis, misalnya stok tidak mencukupi, "
                            + "qty melebihi qty pesanan, atau penyetuju tidak berwenang untuk nilai dokumen."),
            new ProblemDescriptor(ProblemTypes.INVALID_STATE, ProblemTitles.INVALID_STATE, 409,
                    "Status dokumen tidak memungkinkan transisi yang diminta, "
                            + "misalnya menyetujui dokumen yang sudah ditolak."),
            new ProblemDescriptor(ProblemTypes.DUPLICATE_RESOURCE, ProblemTitles.DUPLICATE_RESOURCE, 409,
                    "Kunci unik (SKU, kode, username) sudah digunakan oleh data lain."),
            new ProblemDescriptor(ProblemTypes.VALIDATION_FAILED, ProblemTitles.VALIDATION_FAILED, 400,
                    "Isi body request tidak valid. Lihat extension 'errors' untuk detail per field."),
            new ProblemDescriptor(ProblemTypes.PARAMETER_MISMATCH, ProblemTitles.PARAMETER_MISMATCH, 400,
                    "Parameter request tidak dapat dikonversi ke tipe yang diharapkan."),
            new ProblemDescriptor(ProblemTypes.INVALID_SORT, ProblemTitles.INVALID_SORT, 400,
                    "Properti pada parameter sort tidak ada dalam allowlist endpoint."),
            new ProblemDescriptor(ProblemTypes.INVALID_API_VERSION, ProblemTitles.INVALID_API_VERSION, 400,
                    "Versi API pada header X-API-Version tidak valid atau tidak didukung."),
            new ProblemDescriptor(ProblemTypes.MALFORMED_REQUEST, ProblemTitles.MALFORMED_REQUEST, 400,
                    "Body request bukan JSON yang valid."),
            new ProblemDescriptor(ProblemTypes.METHOD_NOT_ALLOWED, ProblemTitles.METHOD_NOT_ALLOWED, 405,
                    "HTTP method tidak didukung untuk endpoint tersebut."),
            new ProblemDescriptor(ProblemTypes.BAD_REQUEST, ProblemTitles.BAD_REQUEST, 400,
                    "Permintaan tidak valid (kesalahan umum sisi client)."),
            new ProblemDescriptor(ProblemTypes.NOT_ACCEPTABLE, ProblemTitles.NOT_ACCEPTABLE, 406,
                    "Format response yang diminta melalui header Accept tidak didukung."),
            new ProblemDescriptor(ProblemTypes.UNSUPPORTED_MEDIA_TYPE, ProblemTitles.UNSUPPORTED_MEDIA_TYPE, 415,
                    "Content-Type request tidak didukung."),
            new ProblemDescriptor(ProblemTypes.UNAUTHORIZED, ProblemTitles.UNAUTHORIZED, 401,
                    "Token tidak valid, kedaluwarsa, atau tidak disertakan."),
            new ProblemDescriptor(ProblemTypes.FORBIDDEN, ProblemTitles.FORBIDDEN, 403,
                    "User terautentikasi tidak memiliki hak untuk melakukan aksi tersebut."),
            new ProblemDescriptor(ProblemTypes.LOGIN_FAILED, ProblemTitles.LOGIN_FAILED, 401,
                    "Kredensial login salah."),
            new ProblemDescriptor(ProblemTypes.INTERNAL_ERROR, ProblemTitles.INTERNAL_ERROR, 500,
                    "Kesalahan tak terduga di server. Laporkan ke administrator beserta correlationId."));

    private ProblemCatalog() {
    }

    public static List<ProblemDescriptor> all() {
        return DESCRIPTORS;
    }

    public static Optional<ProblemDescriptor> find(String slug) {
        return DESCRIPTORS.stream().filter(descriptor -> descriptor.slug().equals(slug)).findFirst();
    }
}
