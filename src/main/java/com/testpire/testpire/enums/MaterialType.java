package com.testpire.testpire.enums;

/**
 * Kind of teaching material attached to a topic.
 *
 * <ul>
 *   <li>{@link #PDF}, {@link #PPT}, {@link #VIDEO} — file-backed: the bytes live in S3 and the row
 *       carries {@code s3Key}/{@code fileName}/{@code contentType}/{@code sizeBytes}.</li>
 *   <li>{@link #NOTE} — inline rich text: the row carries {@code content} + {@code contentFormat}
 *       (a {@link TextFormat}); no S3 object.</li>
 *   <li>{@link #LINK} — an external URL (e.g. a hosted video / reference); the row carries
 *       {@code externalUrl}; no S3 object.</li>
 * </ul>
 */
public enum MaterialType {
    PDF(true),
    PPT(true),
    VIDEO(true),
    NOTE(false),
    LINK(false);

    private final boolean fileBacked;

    MaterialType(boolean fileBacked) {
        this.fileBacked = fileBacked;
    }

    /** True when this material's payload is an object stored in S3 (vs. inline text or an external URL). */
    public boolean isFileBacked() {
        return fileBacked;
    }
}
