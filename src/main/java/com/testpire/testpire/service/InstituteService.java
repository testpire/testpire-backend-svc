package com.testpire.testpire.service;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.dto.InstituteDto;
import com.testpire.testpire.dto.request.InstituteSearchRequestDto;
import com.testpire.testpire.dto.response.InstituteListResponseDto;
import com.testpire.testpire.dto.response.InstituteResponseDto;
import com.testpire.testpire.entity.Institute;
import com.testpire.testpire.repository.InstituteRepository;
import com.testpire.testpire.repository.specification.InstituteSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            throw new IllegalArgumentException(String.format(ApplicationConstants.Messages.INSTITUTE_ALREADY_EXISTS, instituteDto.code()));
        }
        
        // Check if email already exists
        if (instituteRepository.existsByEmail(instituteDto.email())) {
            throw new IllegalArgumentException(String.format(ApplicationConstants.Messages.INSTITUTE_EMAIL_EXISTS, instituteDto.email()));
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

    public boolean instituteExistsByCode(String code) {
        return instituteRepository.existsByCode(code);
    }

    public boolean instituteExistsById(Long id) {
        return instituteRepository.existsById(id);
    }

    public InstituteListResponseDto searchInstitutesWithSpecification(InstituteSearchRequestDto request) {
        log.info("Searching institutes with specification: {}", request);
        
        // Build specification
        Specification<Institute> spec = Specification.where(InstituteSpecification.hasSearchText(request.getCriteria().getSearchText()))
            .and(InstituteSpecification.hasInstituteId(request.getCriteria().getInstituteId()))
                .and(InstituteSpecification.hasNameContaining(request.getCriteria().getName()))
                .and(InstituteSpecification.hasCodeContaining(request.getCriteria().getCode()))
                .and(InstituteSpecification.hasAddressContaining(request.getCriteria().getAddress()))
                .and(InstituteSpecification.hasCity(request.getCriteria().getCity()))
                .and(InstituteSpecification.hasState(request.getCriteria().getState()))
                .and(InstituteSpecification.hasCountry(request.getCriteria().getCountry()))
                .and(InstituteSpecification.hasPostalCodeContaining(request.getCriteria().getPostalCode()))
                .and(InstituteSpecification.hasPhoneContaining(request.getCriteria().getPhone()))
                .and(InstituteSpecification.hasEmailContaining(request.getCriteria().getEmail()))
                .and(InstituteSpecification.hasWebsiteContaining(request.getCriteria().getWebsite()))
                .and(InstituteSpecification.hasDescriptionContaining(request.getCriteria().getDescription()))
                .and(InstituteSpecification.isActive(request.getCriteria().getActive()))
                .and(InstituteSpecification.createdAfter(request.getCriteria().getCreatedAfter()))
                .and(InstituteSpecification.createdBefore(request.getCriteria().getCreatedBefore()))
                .and(InstituteSpecification.createdBy(request.getCriteria().getCreatedBy()))
                .and(InstituteSpecification.updatedBy(request.getCriteria().getUpdatedBy()));
        
        // Create pageable
        Sort sort = Sort.by(
            Sort.Direction.fromString(request.getSorting().getDirection()),
            request.getSorting().getField()
        );
        Pageable pageable = PageRequest.of(request.getPagination().getPage(), request.getPagination().getSize(), sort);
        
        // Execute query
        Page<Institute> page = instituteRepository.findAll(spec, pageable);
        log.info("Found {} institutes out of {} total", page.getContent().size(), page.getTotalElements());
        
        // Convert to DTOs
        List<InstituteResponseDto> instituteDtos = page.getContent().stream()
                .map(InstituteResponseDto::fromEntity)
                .toList();
        
        return InstituteListResponseDto.success(
            instituteDtos,
            instituteDtos.size(),
            page.getNumber(),
            (int) page.getTotalElements()
        );
    }
} 