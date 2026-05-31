package com.testpire.testpire.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugUtilsTest {

    @Test
    void lowercasesAndDashesSpaces() {
        assertThat(SlugUtils.slugify("Projectile Motion", 1L)).isEqualTo("projectile-motion");
    }

    @Test
    void replacesSlashesAndSpecialChars() {
        assertThat(SlugUtils.slugify("Newton's 2nd Law / Friction", 1L)).isEqualTo("newton-s-2nd-law-friction");
    }

    @Test
    void collapsesAndTrimsSeparators() {
        assertThat(SlugUtils.slugify("  Heat & Thermodynamics  ", 1L)).isEqualTo("heat-thermodynamics");
    }

    @Test
    void stripsAccents() {
        assertThat(SlugUtils.slugify("Équations", 1L)).isEqualTo("equations");
    }

    @Test
    void fallsBackToIdForNullOrBlank() {
        assertThat(SlugUtils.slugify(null, 7L)).isEqualTo("id_7");
        assertThat(SlugUtils.slugify("   ", 7L)).isEqualTo("id_7");
    }

    @Test
    void fallsBackToIdForNonLatinNames() {
        assertThat(SlugUtils.slugify("भौतिक विज्ञान", 42L)).isEqualTo("id_42");
    }

    @Test
    void capsLengthAndTrimsTrailingDash() {
        String slug = SlugUtils.slugify("a".repeat(80), 1L);
        assertThat(slug.length()).isLessThanOrEqualTo(60);
        assertThat(slug).doesNotEndWith("-");
    }
}
