package com.testpire.testpire.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parses the {@code ?include=} query parameter used by hierarchy read endpoints to opt into
 * nested children (e.g. {@code include=subjects,chapters,topics}). Tokens are trimmed,
 * lower-cased and de-duplicated; unknown tokens are simply ignored by the consuming mapper.
 */
public final class IncludeUtils {

    private IncludeUtils() {
    }

    public static Set<String> parse(String include) {
        if (include == null || include.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(include.split(","))
                .map(token -> token.trim().toLowerCase())
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
