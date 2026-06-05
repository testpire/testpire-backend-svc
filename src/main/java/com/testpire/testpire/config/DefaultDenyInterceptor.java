package com.testpire.testpire.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testpire.testpire.annotation.RequirePermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;

/**
 * Default-deny baseline for the API.
 *
 * <p>Authorization in this service is opt-in via {@link RequirePermission} (enforced by
 * {@code AuthorizationAspect}). Because there is no Spring Security filter chain, any controller
 * endpoint that simply forgets the annotation would be silently reachable with no authentication.
 * This interceptor closes that gap: a request whose handler has neither {@link RequirePermission} nor
 * a matching entry in the explicit public allowlist is rejected with 401.</p>
 *
 * <p>It deliberately performs NO role/JWT validation of its own — annotated endpoints continue to
 * be authenticated and authorized by the aspect. This only changes the default for UNannotated
 * endpoints from "public" to "denied".</p>
 */
@Component
@Slf4j
public class DefaultDenyInterceptor implements HandlerInterceptor {

  /** Endpoints intentionally reachable without authentication. Keep this list tight. */
  private static final List<PublicEndpoint> PUBLIC_ENDPOINTS = List.of(
      new PublicEndpoint(HttpMethod.POST, "/api/auth/login"),
      new PublicEndpoint(HttpMethod.POST, "/api/auth/set-password"),
      new PublicEndpoint(HttpMethod.POST, "/api/auth/forgot-password"),
      new PublicEndpoint(HttpMethod.POST, "/api/auth/confirm-forgot-password"),
      // Signup-by-code flow resolves an institute code; path variable follows the prefix.
      new PublicEndpoint(HttpMethod.GET, "/api/institutes/code/")
  );

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // Allow CORS preflight requests.
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      return true;
    }

    // Non-controller handlers (static resources, error dispatch, springdoc, etc.) are out of scope.
    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }

    // Annotated endpoints are authenticated/authorized by AuthorizationAspect — let them through.
    if (handlerMethod.hasMethodAnnotation(RequirePermission.class)) {
      return true;
    }

    // Explicitly-public endpoints.
    if (isPublic(request)) {
      return true;
    }

    // Default deny: the endpoint is neither annotated with @RequirePermission nor allowlisted.
    log.warn("Default-deny blocked unannotated/non-public endpoint: {} {}",
        request.getMethod(), request.getRequestURI());
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), Map.of(
        "error", "Access Denied",
        "message", "This endpoint requires authentication"));
    return false;
  }

  private boolean isPublic(HttpServletRequest request) {
    String uri = request.getRequestURI();
    for (PublicEndpoint endpoint : PUBLIC_ENDPOINTS) {
      if (!endpoint.method().matches(request.getMethod())) {
        continue;
      }
      if (endpoint.path().endsWith("/")) {
        if (uri.startsWith(endpoint.path())) {
          return true; // prefix match (e.g. /api/institutes/code/{code})
        }
      } else if (uri.equals(endpoint.path())) {
        return true;
      }
    }
    return false;
  }

  private record PublicEndpoint(HttpMethod method, String path) {}
}
