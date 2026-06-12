package com.testpire.testpire.repository;

import com.testpire.testpire.entity.TopicMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicMaterialRepository extends JpaRepository<TopicMaterial, Long> {

    // Institute-scoped listing (non-SUPER_ADMIN callers).
    List<TopicMaterial> findByTopicIdAndInstituteIdOrderBySortOrderAscIdAsc(Long topicId, Long instituteId);

    // Unscoped listing (SUPER_ADMIN, whose resolved instituteId is null).
    List<TopicMaterial> findByTopicIdOrderBySortOrderAscIdAsc(Long topicId);

    // Institute-scoped single fetch; a material in another institute is reported as not found.
    Optional<TopicMaterial> findByIdAndInstituteId(Long id, Long instituteId);
}
