package com.my.lostfound.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.NOT_FOUND.value());
        errorBody.put("error", "Not Found");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("error", "Bad Request");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.UNAUTHORIZED.value());
        errorBody.put("error", "Unauthorized");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        log.warn("Forbidden: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.FORBIDDEN.value());
        errorBody.put("error", "Forbidden");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Map<String, Object>> handleFileStorageError(FileStorageException ex) {
        log.error("File storage error: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorBody.put("error", "Internal Server Error");
        errorBody.put("message", "An error occurred while uploading the file. Please try again later.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralError(Exception ex) {
        log.error("An unexpected error occurred", ex);

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorBody.put("error", "Internal Server Error");
        errorBody.put("message", "An unexpected error occurred. Please try again later.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}
