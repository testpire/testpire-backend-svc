package com.testpire.testpire.entity;

import com.testpire.testpire.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "student_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "course", length = 100)
    private String course;

    @Column(name = "current_class")
    private Integer currentClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 16)
    private Gender gender;

    @Column(name = "roll_number", length = 50)
    private String rollNumber;
    
    @Column(name = "parent_name", length = 100)
    private String parentName;
    
    @Column(name = "parent_phone", length = 20)
    private String parentPhone;
    
    @Column(name = "parent_email", length = 100)
    private String parentEmail;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "blood_group", length = 10)
    private String bloodGroup;
    
    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;
    
    @Builder.Default
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
    
    @Builder.Default
    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
