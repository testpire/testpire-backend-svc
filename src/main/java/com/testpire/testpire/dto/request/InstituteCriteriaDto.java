package com.testpire.testpire.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstituteCriteriaDto {

    private Long instituteId;
    private String searchText;
    private String name;
    private String code;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phone;
    private String email;
    private String website;
    private String description;
    private Boolean active;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String createdBy;
    private String updatedBy;
}
