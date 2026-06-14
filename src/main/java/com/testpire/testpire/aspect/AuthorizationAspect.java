package com.testpire.testpire.aspect;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.PermissionService;
import com.testpire.testpire.util.JwksJwtUtil;
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

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationAspect {

  private final JwksJwtUtil jwtUtil;
  private final CognitoService cognitoService;
  private final PermissionService permissionService;

  @Around("@annotation(requirePermission)")
  public Object authorize(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
    try {
      // Resolve and authenticate the caller; sets currentUser/currentUsername request attributes.
      Object resolved = resolveCurrentUser();
      if (resolved instanceof ResponseEntity<?>) {
        return resolved; // authentication failure
      }
      UserDto user = (UserDto) resolved;

      log.info("Authorizing user: {} with role: {} for required permission(s): {}",
          user.username(), user.role(), Arrays.toString(requirePermission.value()));

      if (!permissionService.hasPermission(user.role(), requirePermission.value(),
          requirePermission.requireAll())) {
        log.warn("Access DENIED for user: {} with role: {} trying to access endpoint requiring: {}",
            user.username(), user.role(), Arrays.toString(requirePermission.value()));
        return forbidden("You do not have permission to perform this action.");
      }

      log.info("Access GRANTED for user: {} with role: {}", user.username(), user.role());
      return joinPoint.proceed();

    } catch (Exception e) {
      log.error("Authorization error", e);
      return unauthorized("Authentication failed.");
    }
  }

  /**
   * Validates the bearer token, loads the user from Cognito and stashes it on the request.
   *
   * @return the resolved {@link UserDto} on success, or a 403 {@link ResponseEntity} on failure.
   */
  private Object resolveCurrentUser() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      return unauthorized("Unable to resolve the request context.");
    }

    HttpServletRequest request = attributes.getRequest();
    String authHeader = request.getHeader(ApplicationConstants.Headers.AUTHORIZATION);

    if (authHeader == null ||
        !authHeader.startsWith(ApplicationConstants.Headers.BEARER_PREFIX)) {
      return unauthorized("Authorization header is missing or invalid.");
    }

    String token = authHeader.substring(ApplicationConstants.Headers.BEARER_PREFIX.length());

    if (!jwtUtil.isTokenValid(token)) {
      return unauthorized("Invalid or expired token.");
    }

    String username = jwtUtil.extractUsername(token);
    log.info("Extracted username from JWT: {}", username);

    UserDto user = cognitoService.getUser(username);
    log.info("Retrieved user from Cognito: username={}, role={}, email={}",
        user.username(), user.role(), user.email());

    // Add user info to request attributes for use in controllers
    request.setAttribute("currentUser", user);
    request.setAttribute("currentUsername", username);
    return user;
  }

  /** 401 -- caller is not authenticated (missing/invalid token, unresolvable identity). */
  private ResponseEntity<ApiResponseDto> unauthorized(String message) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.error(message));
  }

  /** 403 -- caller is authenticated but lacks the required permission. */
  private ResponseEntity<ApiResponseDto> forbidden(String message) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponseDto.error(message));
  }
}
