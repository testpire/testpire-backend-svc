package com.testpire.testpire.dto;

import com.testpire.testpire.constants.ApplicationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record InstituteDto(
    @NotBlank(message = ApplicationConstants.Messages.INSTITUTE_NAME_REQUIRED)
    @Size(min = ApplicationConstants.Validation.INSTITUTE_NAME_MIN_LENGTH, max = ApplicationConstants.Validation.INSTITUTE_NAME_MAX_LENGTH, message = "Institute name must be between " + ApplicationConstants.Validation.INSTITUTE_NAME_MIN_LENGTH + " and " + ApplicationConstants.Validation.INSTITUTE_NAME_MAX_LENGTH + " characters")
    String name,
    
    @NotBlank(message = ApplicationConstants.Messages.INSTITUTE_CODE_REQUIRED)
    @Size(min = ApplicationConstants.Validation.INSTITUTE_CODE_MIN_LENGTH, max = ApplicationConstants.Validation.INSTITUTE_CODE_MAX_LENGTH, message = "Institute code must be between " + ApplicationConstants.Validation.INSTITUTE_CODE_MIN_LENGTH + " and " + ApplicationConstants.Validation.INSTITUTE_CODE_MAX_LENGTH + " characters")
    String code,
    
    @NotBlank(message = ApplicationConstants.Messages.ADDRESS_REQUIRED)
    @Size(max = ApplicationConstants.Validation.ADDRESS_MAX_LENGTH, message = "Address must not exceed " + ApplicationConstants.Validation.ADDRESS_MAX_LENGTH + " characters")
    String address,
    
    @NotBlank(message = ApplicationConstants.Messages.CITY_REQUIRED)
    @Size(max = ApplicationConstants.Validation.CITY_MAX_LENGTH, message = "City must not exceed " + ApplicationConstants.Validation.CITY_MAX_LENGTH + " characters")
    String city,
    
    @NotBlank(message = ApplicationConstants.Messages.STATE_REQUIRED)
    @Size(max = ApplicationConstants.Validation.STATE_MAX_LENGTH, message = "State must not exceed " + ApplicationConstants.Validation.STATE_MAX_LENGTH + " characters")
    String state,
    
    @NotBlank(message = ApplicationConstants.Messages.COUNTRY_REQUIRED)
    @Size(max = ApplicationConstants.Validation.COUNTRY_MAX_LENGTH, message = "Country must not exceed " + ApplicationConstants.Validation.COUNTRY_MAX_LENGTH + " characters")
    String country,
    
    @NotBlank(message = ApplicationConstants.Messages.POSTAL_CODE_REQUIRED)
    @Size(max = ApplicationConstants.Validation.POSTAL_CODE_MAX_LENGTH, message = "Postal code must not exceed " + ApplicationConstants.Validation.POSTAL_CODE_MAX_LENGTH + " characters")
    String postalCode,
    
    @NotBlank(message = ApplicationConstants.Messages.PHONE_REQUIRED)
    @Size(max = ApplicationConstants.Validation.PHONE_MAX_LENGTH, message = "Phone number must not exceed " + ApplicationConstants.Validation.PHONE_MAX_LENGTH + " characters")
    String phone,
    
    @Email(message = "Email must be valid")
    @NotBlank(message = ApplicationConstants.Messages.EMAIL_REQUIRED)
    String email,
    
    String website,
    
    String description
) {} 