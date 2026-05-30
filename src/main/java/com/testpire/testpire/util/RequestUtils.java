package com.testpire.testpire.util;

import com.testpire.testpire.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestUtils {
    
    public static UserDto getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (UserDto) request.getAttribute("currentUser");
        }
        return null;
    }
    
    public static String getCurrentUsername() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (String) request.getAttribute("currentUsername");
        }
        return null;
    }
    
    public static Long getCurrentUserInstituteId() {
        UserDto currentUser = getCurrentUser();
        if (currentUser == null || currentUser.instituteId() == null || currentUser.instituteId() < 1) {
            return null;
        }
        return currentUser.instituteId();
    }

    /**
     * Reads the {@code X-Institute-Id} request header and parses it to a Long.
     * Returns null if the header is absent, blank, or non-numeric.
     *
     * <p>This is intentionally only consulted by {@link #resolveInstituteId(Long)} as the
     * SUPER_ADMIN fallback — it must never be trusted for non-SUPER_ADMIN users, whose
     * institute is always determined by their JWT.</p>
     */
    public static Long getActingInstituteIdHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String header = request.getHeader("X-Institute-Id");
        if (header == null || header.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Returns the JWT-scoped instituteId for non-SUPER_ADMIN users (enforcing multi-tenancy),
     * and falls back to the request-provided value for SUPER_ADMIN (who operates across institutes).
     *
     * <p>For SUPER_ADMIN (whose JWT institute is null) the resolution order is:
     * the {@code X-Institute-Id} acting-institute header first, then the request/criteria value.
     * For non-SUPER_ADMIN the JWT institute is non-null and is always returned as-is, so the
     * header and any client-supplied value are ignored — preserving multi-tenancy isolation.</p>
     */
    public static Long resolveInstituteId(Long requestInstituteId) {
        Long jwtInstituteId = getCurrentUserInstituteId();
        if (jwtInstituteId != null) {
            return jwtInstituteId;
        }
        Long headerInstituteId = getActingInstituteIdHeader();
        return headerInstituteId != null ? headerInstituteId : requestInstituteId;
    }
}
