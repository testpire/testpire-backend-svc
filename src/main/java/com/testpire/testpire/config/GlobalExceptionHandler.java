package com.testpire.testpire.config;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.dto.response.ApiResponseDto;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Single source of truth for HTTP error responses. Every handler returns the {@link ApiResponseDto}
 * envelope ({@code success}/{@code message}, plus a field -> message {@code errors} map for
 * validation failures) so clients see one consistent shape. Internal details (class names, stack
 * traces, raw exception messages from unexpected faults) are logged server-side, never returned.
 *
 * <p>Status mapping: 400 for bad/invalid input, 409 for conflicts/business-rule violations, 500 for
 * anything unexpected (generic message only).
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private static final String VALIDATION_FAILED = "Validation failed";

  /**
   * Bean-validation failure on a {@code @Valid @RequestBody} argument -> 400. Returns a clean
   * field -> message map instead of Spring's internal exception dump (which used to leak out as a
   * 500). First message wins per field.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponseDto> handleValidation(MethodArgumentNotValidException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    e.getBindingResult().getFieldErrors()
        .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
    e.getBindingResult().getGlobalErrors()
        .forEach(ge -> errors.putIfAbsent(ge.getObjectName(), ge.getDefaultMessage()));
    log.warn("request body validation failed: {}", errors);
    return ResponseEntity.badRequest().body(ApiResponseDto.validationError(VALIDATION_FAILED, errors));
  }

  /**
   * Validation failure on individual handler parameters ({@code @RequestParam}/{@code @PathVariable}
   * with constraints) under Spring 6.1+ -> 400.
   */
  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ApiResponseDto> handleHandlerMethodValidation(HandlerMethodValidationException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    e.getParameterValidationResults().forEach(result -> {
      String field = result.getMethodParameter().getParameterName();
      String key = field != null ? field : "request";
      result.getResolvableErrors()
          .forEach(err -> errors.putIfAbsent(key, err.getDefaultMessage()));
    });
    log.warn("parameter validation failed: {}", errors);
    return ResponseEntity.badRequest().body(ApiResponseDto.validationError(VALIDATION_FAILED, errors));
  }

  /**
   * Constraint violation on a {@code @Validated} controller's method parameters -> 400. The property
   * path is trimmed to the bare field name so the client sees {@code "email"}, not
   * {@code "createUser.arg0.email"}.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponseDto> handleConstraintViolation(ConstraintViolationException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    e.getConstraintViolations().forEach(v -> {
      String path = v.getPropertyPath().toString();
      String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
      errors.putIfAbsent(field, v.getMessage());
    });
    log.warn("constraint violation: {}", errors);
    return ResponseEntity.badRequest().body(ApiResponseDto.validationError(VALIDATION_FAILED, errors));
  }

  /**
   * Bad input rejected programmatically (services throw {@link IllegalArgumentException} for invalid
   * arguments and "not found" lookups) -> 400. These messages are authored in our own code and are
   * safe to surface.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponseDto> handleIllegalArgument(IllegalArgumentException e) {
    log.warn("illegal argument: {}", e.getMessage());
    return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
  }

  /**
   * A delete blocked by a FK (e.g. deleting a question still referenced by a test or recorded answer,
   * including via cascade from its topic/institute -- see V27) surfaces as a DataIntegrityViolation.
   * Report it as 409 Conflict rather than a 500.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException e) {
    log.warn("data integrity violation", e);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponseDto.error("The operation conflicts with existing data and was rejected. "
            + "It likely references rows that are still in use (e.g. a question used by a test or attempt)."));
  }

  /** Business-rule violations (e.g. deleting an in-use question) -> 409 Conflict. */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponseDto> handleIllegalState(IllegalStateException e) {
    log.warn("illegal state: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseDto.error(e.getMessage()));
  }

  /** Anything unexpected -> 500 with a generic message; the real cause is logged, never returned. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponseDto> handleException(Exception e) {
    log.error("unhandled error", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponseDto.error(ApplicationConstants.Messages.INTERNAL_SERVER_ERROR));
  }
}
