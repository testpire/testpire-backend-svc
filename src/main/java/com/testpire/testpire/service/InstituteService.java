package com.testpire.testpire.service;

import com.testpire.testpire.dto.InstituteDto;
import com.testpire.testpire.mongoDomain.Institute;
import com.testpire.testpire.repository.InstituteRepository;
import com.testpire.testpire.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InstituteService {

    private final InstituteRepository instituteRepository;

    public Institute createInstitute(InstituteDto instituteDto, String createdBy) {
        log.info("Creating institute: {}", instituteDto.name());
        
        // Check if institute code already exists
        if (instituteRepository.existsByCode(instituteDto.code())) {
            throw new IllegalArgumentException("Institute with code " + instituteDto.code() + " already exists");
        }
        
        // Check if email already exists
        if (instituteRepository.existsByEmail(instituteDto.email())) {
            throw new IllegalArgumentException("Institute with email " + instituteDto.email() + " already exists");
        }

        Institute institute = Institute.builder()
                .code(instituteDto.code())
                .name(instituteDto.name())
                .address(instituteDto.address())
                .city(instituteDto.city())
                .state(instituteDto.state())
                .country(instituteDto.country())
                .postalCode(instituteDto.postalCode())
                .phone(instituteDto.phone())
                .email(instituteDto.email())
                .website(instituteDto.website())
                .description(instituteDto.description())
                .active(true)
                .createdBy(createdBy)
                .build();

        Institute savedInstitute = instituteRepository.save(institute);
        log.info("Institute created successfully with ID: {}", savedInstitute.getId());
        return savedInstitute;
    }

    public Institute updateInstitute(Long id, InstituteDto instituteDto, String updatedBy) {
        log.info("Updating institute with ID: {}", id);
        
        Institute existingInstitute = instituteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Institute not found with ID: " + id));

        // Check if code is being changed and if new code already exists
        if (!existingInstitute.getCode().equals(instituteDto.code()) && 
            instituteRepository.existsByCode(instituteDto.code())) {
            throw new IllegalArgumentException("Institute with code " + instituteDto.code() + " already exists");
        }

        // Check if email is being changed and if new email already exists
        if (!existingInstitute.getEmail().equals(instituteDto.email()) && 
            instituteRepository.existsByEmail(instituteDto.email())) {
            throw new IllegalArgumentException("Institute with email " + instituteDto.email() + " already exists");
        }

        existingInstitute.setCode(instituteDto.code());
        existingInstitute.setName(instituteDto.name());
        existingInstitute.setAddress(instituteDto.address());
        existingInstitute.setCity(instituteDto.city());
        existingInstitute.setState(instituteDto.state());
        existingInstitute.setCountry(instituteDto.country());
        existingInstitute.setPostalCode(instituteDto.postalCode());
        existingInstitute.setPhone(instituteDto.phone());
        existingInstitute.setEmail(instituteDto.email());
        existingInstitute.setWebsite(instituteDto.website());
        existingInstitute.setDescription(instituteDto.description());
        existingInstitute.setUpdatedBy(updatedBy);

        Institute updatedInstitute = instituteRepository.save(existingInstitute);
        log.info("Institute updated successfully with ID: {}", updatedInstitute.getId());
        return updatedInstitute;
    }

    public void deleteInstitute(Long id) {
        log.info("Deleting institute with ID: {}", id);
        
        Institute institute = instituteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Institute not found with ID: " + id));
        
        institute.setActive(false);
        instituteRepository.save(institute);
        log.info("Institute deactivated successfully with ID: {}", id);
    }

    public Institute getInstituteById(Long id) {
        return instituteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Institute not found with ID: " + id));
    }

    public Institute getInstituteByCode(String code) {
        return instituteRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new IllegalArgumentException("Institute not found with code: " + code));
    }

    public List<Institute> getAllActiveInstitutes() {
        return instituteRepository.findByActiveTrue();
    }

    public List<Institute> searchInstitutes(String searchTerm) {
        return instituteRepository.findByActiveTrueAndNameOrCodeContaining(searchTerm);
    }

    public boolean instituteExists(String code) {
        return instituteRepository.existsByCode(code);
    }

    public boolean instituteExistsById(Long id) {
        return instituteRepository.existsById(id);
    }
} 