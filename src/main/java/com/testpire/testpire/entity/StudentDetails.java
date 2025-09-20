package com.testpire.testpire.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    
    @Column(name = "year_of_study")
    private Integer yearOfStudy;
    
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
    private LocalDateTime dateOfBirth;
    
    @Column(name = "blood_group", length = 10)
    private String bloodGroup;
    
    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;
    
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
