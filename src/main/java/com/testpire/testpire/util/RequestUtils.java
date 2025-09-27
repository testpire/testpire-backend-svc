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
        if (currentUser == null || currentUser.instituteId() < 1){
            //for super admin
            return null;
        }
        return currentUser.instituteId();
    }
}
