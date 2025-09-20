package com.testpire.testpire.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "subject", length = 100)
    private String subject;
    
    @Column(name = "qualification", length = 200)
    private String qualification;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Column(name = "specialization", length = 200)
    private String specialization;
    
    @Column(name = "bio", length = 1000)
    private String bio;
    
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
