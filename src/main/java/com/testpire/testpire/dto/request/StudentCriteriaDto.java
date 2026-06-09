package com.testpire.testpire.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCriteriaDto {
    
    private Long instituteId;
    private String searchText;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phone;
    private String course;
    /** Filter by enrolled course (student_enrollments.course_id) — the enrollment source of truth, not the legacy free-text {@code course}. */
    private Long courseId;
    /** Filter by enrolled batch (student_enrollments.batch_id). */
    private Long batchId;
    private Integer minCurrentClass;
    private Integer maxCurrentClass;
    private String rollNumber;
    private String parentName;
    private String parentPhone;
    private String parentEmail;
    private String address;
    private String bloodGroup;
    private String emergencyContact;
    private Boolean enabled;
    private Instant createdAfter;
    private Instant createdBefore;
    private String createdBy;
}
