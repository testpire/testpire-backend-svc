package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = ApplicationConstants.Database.INSTITUTES_TABLE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Institute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = ApplicationConstants.Messages.INSTITUTE_CODE_REQUIRED)
    @Size(min = ApplicationConstants.Validation.INSTITUTE_CODE_MIN_LENGTH, max = ApplicationConstants.Validation.INSTITUTE_CODE_MAX_LENGTH, message = "Institute code must be between " + ApplicationConstants.Validation.INSTITUTE_CODE_MIN_LENGTH + " and " + ApplicationConstants.Validation.INSTITUTE_CODE_MAX_LENGTH + " characters")
    private String code;
    
    @Column(nullable = false)
    @NotBlank(message = ApplicationConstants.Messages.INSTITUTE_NAME_REQUIRED)
    @Size(min = ApplicationConstants.Validation.INSTITUTE_NAME_MIN_LENGTH, max = ApplicationConstants.Validation.INSTITUTE_NAME_MAX_LENGTH, message = "Institute name must be between " + ApplicationConstants.Validation.INSTITUTE_NAME_MIN_LENGTH + " and " + ApplicationConstants.Validation.INSTITUTE_NAME_MAX_LENGTH + " characters")
    private String name;
    
    @Column(length = ApplicationConstants.Validation.ADDRESS_MAX_LENGTH)
    private String address;
    
    @Size(max = ApplicationConstants.Validation.CITY_MAX_LENGTH, message = "City must not exceed " + ApplicationConstants.Validation.CITY_MAX_LENGTH + " characters")
    private String city;
    
    @Size(max = ApplicationConstants.Validation.STATE_MAX_LENGTH, message = "State must not exceed " + ApplicationConstants.Validation.STATE_MAX_LENGTH + " characters")
    private String state;
    
    @Size(max = ApplicationConstants.Validation.COUNTRY_MAX_LENGTH, message = "Country must not exceed " + ApplicationConstants.Validation.COUNTRY_MAX_LENGTH + " characters")
    private String country;
    
    @Size(max = ApplicationConstants.Validation.POSTAL_CODE_MAX_LENGTH, message = "Postal code must not exceed " + ApplicationConstants.Validation.POSTAL_CODE_MAX_LENGTH + " characters")
    private String postalCode;
    
    @Size(max = ApplicationConstants.Validation.PHONE_MAX_LENGTH, message = "Phone number must not exceed " + ApplicationConstants.Validation.PHONE_MAX_LENGTH + " characters")
    private String phone;
    
    @Column(unique = true)
    private String email;
    
    private String website;
    
    @Column(length = ApplicationConstants.Validation.DESCRIPTION_MAX_LENGTH)
    private String description;
    
    @Builder.Default
    private boolean active = true;
    
    @Builder.Default
    @Column(name = ApplicationConstants.Database.CREATED_AT_COLUMN)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    @Column(name = ApplicationConstants.Database.UPDATED_AT_COLUMN)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = ApplicationConstants.Database.CREATED_BY_COLUMN)
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 