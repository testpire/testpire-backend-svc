package com.testpire.testpire.aspect;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationAspect {

    private final JwtUtil jwtUtil;
    private final CognitoService cognitoService;

    @Around("@annotation(requireRole)")
    public Object authorize(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        try {
            // Get the HTTP request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return createErrorResponse("Unable to get request context");
            }
            
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader(ApplicationConstants.Headers.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith(ApplicationConstants.Headers.BEARER_PREFIX)) {
                return createErrorResponse("Authorization header missing or invalid");
            }
            
            // Extract and validate JWT token
            String token = authHeader.substring(ApplicationConstants.Headers.BEARER_PREFIX.length());
            
            if (!jwtUtil.isTokenValid(token)) {
                return createErrorResponse("Invalid or expired token");
            }
            
            // Extract username and get user details
            String username = jwtUtil.extractUsername(token);
            log.info("Extracted username from JWT: {}", username);
            
            UserDto user = cognitoService.getUser(username);
            log.info("Retrieved user from Cognito: username={}, role={}, email={}", 
                    user.username(), user.role(), user.email());
            
            log.info("Authorizing user: {} with role: {} for required roles: {}", 
                    username, user.role(), Arrays.toString(requireRole.value()));
            
            // Check if user has required role(s)
            if (!hasRequiredRole(user.role(), requireRole.value(), requireRole.requireAll())) {
                log.warn("Access DENIED for user: {} with role: {} trying to access endpoint requiring: {}", 
                        username, user.role(), Arrays.toString(requireRole.value()));
                return createErrorResponse("Insufficient permissions. Required role(s): " + 
                        Arrays.toString(requireRole.value()) + ", but user has: " + user.role());
            }
            
            log.info("Access GRANTED for user: {} with role: {}", username, user.role());
            
            // Add user info to request attributes for use in controller
            request.setAttribute("currentUser", user);
            request.setAttribute("currentUsername", username);
            
            // Proceed with the method execution
            return joinPoint.proceed();
            
        } catch (Exception e) {
            log.error("Authorization error", e);
            return createErrorResponse("Authorization failed: " + e.getMessage());
        }
    }
    
    private boolean hasRequiredRole(UserRole userRole, UserRole[] requiredRoles, boolean requireAll) {
        if (requiredRoles.length == 0) {
            return true; // No specific role required
        }
        
        if (requireAll) {
            // User must have ALL required roles
            return Arrays.stream(requiredRoles)
                    .allMatch(requiredRole -> hasRoleOrHigher(userRole, requiredRole));
        } else {
            // User needs ANY of the required roles
            return Arrays.stream(requiredRoles)
                    .anyMatch(requiredRole -> hasRoleOrHigher(userRole, requiredRole));
        }
    }
    
    private boolean hasRoleOrHigher(UserRole userRole, UserRole requiredRole) {
        // Define role hierarchy: SUPER_ADMIN > INST_ADMIN > TEACHER > STUDENT
        switch (userRole) {
            case SUPER_ADMIN:
                return true; // SUPER_ADMIN can access everything
            case INST_ADMIN:
                return requiredRole == UserRole.INST_ADMIN || requiredRole == UserRole.TEACHER || requiredRole == UserRole.STUDENT;
            case TEACHER:
                return requiredRole == UserRole.TEACHER || requiredRole == UserRole.STUDENT;
            case STUDENT:
                return requiredRole == UserRole.STUDENT;
            default:
                return false;
        }
    }
    
    private ResponseEntity<Map<String, String>> createErrorResponse(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access Denied", "message", message));
    }
}
