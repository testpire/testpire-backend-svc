package com.testpire.testpire.repository.specification;

import com.testpire.testpire.entity.Institute;
import jakarta.persistence.criteria.Predicate;
import java.util.Objects;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InstituteSpecification {

    public static Specification<Institute> hasSearchText(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchText)) {
                return criteriaBuilder.conjunction();
            }
            
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("state")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("country")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("website")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
            );
        };
    }

    public static Specification<Institute> hasInstituteId(Long id) {
        return (root, query, criteriaBuilder) -> {
            if (Objects.isNull(id)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    public static Specification<Institute> hasNameContaining(String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> hasCodeContaining(String code) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(code)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), "%" + code.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> hasAddressContaining(String address) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(address)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + address.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> hasCity(String city) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(city)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), "%" + city.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> hasState(String state) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(state)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("state")), "%" + state.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> hasCountry(String country) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(country)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("country")), "%" + country.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> hasPostalCodeContaining(String postalCode) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(postalCode)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("postalCode")), "%" + postalCode.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> hasPhoneContaining(String phone) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(phone)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), "%" + phone.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> hasEmailContaining(String email) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(email)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> hasWebsiteContaining(String website) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(website)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("website")), "%" + website.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> hasDescriptionContaining(String description) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(description)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    public static Specification<Institute> createdAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<Institute> createdBefore(LocalDateTime createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<Institute> createdBy(String createdBy) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(createdBy)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("createdBy")), "%" + createdBy.toLowerCase() + "%");
        };
    }

    public static Specification<Institute> updatedBy(String updatedBy) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(updatedBy)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("updatedBy")), "%" + updatedBy.toLowerCase() + "%");
        };
    }
}
