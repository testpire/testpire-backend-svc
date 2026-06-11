package com.testpire.testpire.service;

import com.testpire.testpire.dto.InstituteDto;
import com.testpire.testpire.entity.Institute;
import com.testpire.testpire.repository.InstituteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstituteServiceTest {

    @Mock
    InstituteRepository instituteRepository;

    @InjectMocks
    InstituteService instituteService;

    // InstituteDto(name, code, address, city, state, country, postalCode, phone, email, website, description)
    private static InstituteDto dto(String code, String email) {
        return new InstituteDto("Test Academy", code, "123 St", "City",
                "State", "Country", "12345", "+91111", email, "https://test.com", "Desc");
    }

    private static Institute entity(Long id, String code) {
        return Institute.builder()
                .id(id).name("Test Academy").code(code)
                .email(code.toLowerCase() + "@test.com").build();
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Test
    void createInstitute_uniqueCodeAndEmail_savesAndReturns() {
        when(instituteRepository.existsByCode("TST01")).thenReturn(false);
        when(instituteRepository.existsByEmail("tst@test.com")).thenReturn(false);
        when(instituteRepository.save(any())).thenAnswer(inv -> {
            Institute i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        Institute result = instituteService.createInstitute(dto("TST01", "tst@test.com"), "admin");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("TST01");
        verify(instituteRepository).save(any());
    }

    @Test
    void createInstitute_duplicateCode_throwsAndNeverSaves() {
        when(instituteRepository.existsByCode("TST01")).thenReturn(true);

        assertThatThrownBy(() -> instituteService.createInstitute(dto("TST01", "new@test.com"), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TST01");

        verify(instituteRepository, never()).save(any());
    }

    @Test
    void createInstitute_duplicateEmail_throwsAndNeverSaves() {
        when(instituteRepository.existsByCode("NEW01")).thenReturn(false);
        when(instituteRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> instituteService.createInstitute(dto("NEW01", "dup@test.com"), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dup@test.com");

        verify(instituteRepository, never()).save(any());
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    void updateInstitute_notFound_throwsIllegalArgument() {
        when(instituteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instituteService.updateInstitute(99L, dto("TST01", "t@t.com"), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateInstitute_codeConflict_throwsIllegalArgument() {
        Institute existing = entity(1L, "ORIG01");
        when(instituteRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(instituteRepository.existsByCode("OTHER")).thenReturn(true);

        assertThatThrownBy(() -> instituteService.updateInstitute(1L, dto("OTHER", existing.getEmail()), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTHER");
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    @Test
    void getInstituteById_found_returnsEntity() {
        when(instituteRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "TST01")));

        Institute result = instituteService.getInstituteById(1L);

        assertThat(result.getCode()).isEqualTo("TST01");
    }

    @Test
    void getInstituteById_notFound_throwsIllegalArgument() {
        when(instituteRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instituteService.getInstituteById(42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("42");
    }

    @Test
    void instituteExistsById_found_returnsTrue() {
        when(instituteRepository.existsById(1L)).thenReturn(true);
        assertThat(instituteService.instituteExistsById(1L)).isTrue();
    }

    @Test
    void instituteExistsById_missing_returnsFalse() {
        when(instituteRepository.existsById(99L)).thenReturn(false);
        assertThat(instituteService.instituteExistsById(99L)).isFalse();
    }
}
