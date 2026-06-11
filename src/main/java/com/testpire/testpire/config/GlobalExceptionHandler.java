package com.testpire.testpire.config;

import com.testpire.testpire.constants.ApplicationConstants;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * A delete blocked by a FK (e.g. deleting a question still referenced by a test or recorded answer,
   * including via cascade from its topic/institute — see V27) surfaces as a DataIntegrityViolation.
   * Report it as 409 Conflict rather than a 500.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
    log.warn("data integrity violation", e);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "Conflict",
            "message", "The operation conflicts with existing data and was rejected. "
                + "It likely references rows that are still in use (e.g. a question used by a test or attempt)."));
  }

  /** Business-rule violations (e.g. deleting an in-use question) -> 409 Conflict. */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
    log.warn("illegal state: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "Conflict", "message", e.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleException(Exception e) {
    log.error("error", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", ApplicationConstants.Messages.INTERNAL_SERVER_ERROR, "message", e.getMessage()));
  }
}
