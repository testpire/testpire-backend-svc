package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateCourseRequestDto;
import com.testpire.testpire.dto.request.UpdateCourseRequestDto;
import com.testpire.testpire.dto.response.CourseResponseDto;
import com.testpire.testpire.entity.Course;
import com.testpire.testpire.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    CourseService courseService;

    private static Course entity(Long id, String code) {
        return Course.builder()
                .id(id).name("Physics 101").code(code)
                .instituteId(2L).duration("60 hours").level("BEGINNER")
                .active(true).createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    private static CreateCourseRequestDto createDto(String code) {
        return new CreateCourseRequestDto("Physics 101", null, code, 2L, "60 hours", "BEGINNER", null, null, null);
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Test
    void createCourse_uniqueCode_savesAndReturns() {
        when(courseRepository.existsByCodeAndInstituteId("PHY101", 2L)).thenReturn(false);
        when(courseRepository.save(any())).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CourseResponseDto result = courseService.createCourse(createDto("PHY101"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("PHY101");
        verify(courseRepository).save(any());
    }

    @Test
    void createCourse_duplicateCode_throwsAndNeverSaves() {
        when(courseRepository.existsByCodeAndInstituteId("PHY101", 2L)).thenReturn(true);

        assertThatThrownBy(() -> courseService.createCourse(createDto("PHY101")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PHY101");

        verify(courseRepository, never()).save(any());
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    @Test
    void getCourseById_found_returnsDto() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "PHY101")));

        CourseResponseDto result = courseService.getCourseById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("PHY101");
    }

    @Test
    void getCourseById_notFound_throwsIllegalArgument() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getCourseByCode_found_returnsDto() {
        when(courseRepository.findByCodeAndInstituteId("PHY101", 2L))
                .thenReturn(Optional.of(entity(1L, "PHY101")));

        CourseResponseDto result = courseService.getCourseByCode("PHY101", 2L);

        assertThat(result.code()).isEqualTo("PHY101");
        assertThat(result.instituteId()).isEqualTo(2L);
    }

    @Test
    void getCourseByCode_notFound_throwsIllegalArgument() {
        when(courseRepository.findByCodeAndInstituteId("MISSING", 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseByCode("MISSING", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MISSING");
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    void updateCourse_notFound_throwsIllegalArgument() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.updateCourse(99L,
                new UpdateCourseRequestDto(null, "Updated desc", null, null, null, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateCourse_codeConflict_throwsIllegalArgument() {
        Course existing = entity(1L, "PHY101");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseRepository.existsByCodeAndInstituteId("CHEM101", 2L)).thenReturn(true);

        assertThatThrownBy(() -> courseService.updateCourse(1L,
                new UpdateCourseRequestDto(null, null, "CHEM101", null, null, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CHEM101");
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Test
    void deleteCourse_deactivatesCourse() {
        Course existing = entity(1L, "PHY101");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseRepository.save(any())).thenReturn(existing);

        courseService.deleteCourse(1L);

        assertThat(existing.isActive()).isFalse();
        verify(courseRepository).save(existing);
    }
}
