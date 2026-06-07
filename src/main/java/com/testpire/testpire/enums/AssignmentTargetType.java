package com.testpire.testpire.enums;

/**
 * What a {@link com.testpire.testpire.entity.TestAssignment} points at. The assignment's
 * {@code targetId} is resolved against a different table per type:
 * {@code COURSE} -> courses.id, {@code BATCH} -> batches.id, {@code STUDENT} -> users.id.
 *
 * <p>A COURSE assignment reaches every student enrolled in any batch of that course; a BATCH
 * assignment reaches every student enrolled in that batch; a STUDENT assignment reaches exactly
 * one student. Resolution is dynamic (see {@code TestResolutionService}).</p>
 */
public enum AssignmentTargetType {
    COURSE, BATCH, STUDENT
}
