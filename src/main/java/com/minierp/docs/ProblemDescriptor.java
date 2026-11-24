package com.minierp.docs;

/**
 * One documented problem type, as served by {@code GET /api/problems}.
 */
public record ProblemDescriptor(String slug, String title, int status, String description) {
}
