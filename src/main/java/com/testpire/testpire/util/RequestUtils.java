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
     * Returns the JWT-scoped instituteId for non-SUPER_ADMIN users (enforcing multi-tenancy),
     * and falls back to the request-provided value for SUPER_ADMIN (who operates across institutes).
     */
    public static Long resolveInstituteId(Long requestInstituteId) {
        Long jwtInstituteId = getCurrentUserInstituteId();
        return jwtInstituteId != null ? jwtInstituteId : requestInstituteId;
    }
}
