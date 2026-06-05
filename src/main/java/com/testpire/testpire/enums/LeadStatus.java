package com.testpire.testpire.enums;

/**
 * Lifecycle of an enquiry (lead) before it becomes an enrolled student.
 *
 * <p>The happy path is {@code NEW → CONTACTED → INTERESTED → DEMO_SCHEDULED → ENROLLED}.
 * {@code ENROLLED} is set only by the conversion flow (which provisions the Cognito/User account);
 * it should not be set directly via a plain update. {@code LOST} and {@code NOT_INTERESTED} are
 * terminal closed-lost states.</p>
 */
public enum LeadStatus {
    NEW,
    CONTACTED,
    INTERESTED,
    DEMO_SCHEDULED,
    ENROLLED,
    LOST,
    NOT_INTERESTED
}
