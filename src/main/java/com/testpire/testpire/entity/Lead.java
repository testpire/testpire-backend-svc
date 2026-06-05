package com.testpire.testpire.entity;

import com.testpire.testpire.enums.LeadSource;
import com.testpire.testpire.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An enquiry / prospective student that the institute follows up on before enrollment.
 *
 * <p>A lead deliberately has NO Cognito account and NO {@link User} row — it is a CRM record only.
 * When the lead reaches {@link LeadStatus#ENROLLED} via the conversion flow, a real student
 * (Cognito user + {@code User} + {@code StudentDetails}) is provisioned and linked back here via
 * {@link #convertedUserId} / {@link #enrolledCourseId}. Multi-tenancy is enforced by filtering on
 * {@link #instituteId} in the service/specification layers, as elsewhere in this codebase.</p>
 */
@Entity
@Table(name = "leads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institute_id", nullable = false)
    private Long instituteId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    /** Optional at enquiry time; required when converting (becomes the login username/email). Not unique. */
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 32)
    private LeadStatus status = LeadStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 32)
    private LeadSource source;

    /** Course the lead enquired about (may differ from the course they ultimately enroll in). */
    @Column(name = "interested_course_id")
    private Long interestedCourseId;

    /** Username of the counselor/staff responsible for following up. */
    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;

    @Column(name = "notes", length = 2000)
    private String notes;

    /** Set once the lead is converted: the provisioned student's users.id. Null while still a lead. */
    @Column(name = "converted_user_id")
    private Long convertedUserId;

    /** The course actually enrolled into at conversion. */
    @Column(name = "enrolled_course_id")
    private Long enrolledCourseId;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
