package com.minierp.common;

/**
 * Document number formatting. Numbers are derived from the document id,
 * which is pre-allocated from a database sequence before the insert.
 */
public final class DocumentNumbers {

    private DocumentNumbers() {
    }

    public static String format(String prefix, Long id) {
        if (id == null) {
            throw new IllegalStateException(
                    "Nomor dokumen %s belum dapat dibuat: entity belum disimpan (id belum tersedia)".formatted(prefix));
        }
        return "%s-%06d".formatted(prefix, id.longValue());
    }
}
