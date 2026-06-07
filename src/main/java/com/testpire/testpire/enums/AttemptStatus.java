package com.testpire.testpire.enums;

/**
 * Lifecycle of a {@link com.testpire.testpire.entity.TestAttempt}.
 *
 * <ul>
 *   <li>{@code IN_PROGRESS} — started, answers may still be saved (until {@code expires_at}).</li>
 *   <li>{@code SUBMITTED} — submitted by the student before the deadline.</li>
 *   <li>{@code AUTO_SUBMITTED} — the deadline passed; finalized lazily on the next read.</li>
 *   <li>{@code GRADED} — scoring complete (set together with SUBMITTED/AUTO_SUBMITTED for
 *       objective-only tests, which grade synchronously).</li>
 * </ul>
 */
public enum AttemptStatus {
    IN_PROGRESS, SUBMITTED, AUTO_SUBMITTED, GRADED
}
