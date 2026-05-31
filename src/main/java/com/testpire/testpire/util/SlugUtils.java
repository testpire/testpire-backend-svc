package com.testpire.testpire.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Turns human-readable names (course/chapter/topic) into S3-key-safe path segments.
 * S3 keys are immutable, so a slug is a point-in-time label only — the DB foreign key
 * remains the source of truth for the question's place in the hierarchy.
 */
public final class SlugUtils {

    private static final int MAX_LENGTH = 60;

    private SlugUtils() {
    }

    /**
     * Lowercase, accent-stripped, alphanumeric-and-dash slug. Falls back to {@code id_<fallbackId>}
     * when the name is null/blank or contains no usable characters (e.g. non-Latin scripts), so the
     * returned value is always a valid, non-empty key segment.
     */
    public static String slugify(String name, Long fallbackId) {
        if (name != null && !name.isBlank()) {
            String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "");
            String slug = normalized.toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("(^-+)|(-+$)", "");
            if (slug.length() > MAX_LENGTH) {
                slug = slug.substring(0, MAX_LENGTH).replaceAll("-+$", "");
            }
            if (!slug.isEmpty()) {
                return slug;
            }
        }
        return "id_" + fallbackId;
    }
}
