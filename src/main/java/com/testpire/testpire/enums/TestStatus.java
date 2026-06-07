package com.testpire.testpire.enums;

/**
 * Lifecycle of a {@link com.testpire.testpire.entity.Test}.
 *
 * <ul>
 *   <li>{@code DRAFT} — being curated; not visible to students and not assignable.</li>
 *   <li>{@code PUBLISHED} — finalized; assignable and takeable within its availability window.</li>
 *   <li>{@code CLOSED} — withdrawn; no new attempts may start (existing results are retained).</li>
 * </ul>
 */
public enum TestStatus {
    DRAFT, PUBLISHED, CLOSED
}
