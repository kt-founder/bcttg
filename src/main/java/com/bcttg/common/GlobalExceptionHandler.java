package com.bcttg.common;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.exception.SQLGrammarException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        ApiError error = new ApiError(ex.getCode().name(), ex.getMessage(), ex.getDetails());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.toList());
        ApiError error = new ApiError(ErrorCode.VALIDATION_ERROR.name(), "Validation failed", details);
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.toList());
        ApiError error = new ApiError(ErrorCode.VALIDATION_ERROR.name(), "Validation failed", details);
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(HttpMessageNotReadableException ex) {
        String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "Malformed JSON request";
        ApiError error = new ApiError(ErrorCode.BAD_REQUEST.name(), "Invalid request body", List.of(detail));
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        String detail = ex.getMaxUploadSize() > 0
            ? "Kich thuoc toi da: " + ex.getMaxUploadSize() + " bytes"
            : "Kich thuoc tep vuot gioi han cho phep";
        ApiError error = new ApiError(ErrorCode.BAD_REQUEST.name(), "File upload vuot gioi han cho phep", List.of(detail));
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ApiResponse.error(error));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = ex.getName() + ": invalid value '" + ex.getValue() + "'";
        ApiError error = new ApiError(ErrorCode.BAD_REQUEST.name(), "Invalid request parameter", List.of(detail));
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        if (ex instanceof DisabledException) {
            ApiError error = new ApiError(ErrorCode.FORBIDDEN.name(), "User is inactive", List.of());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(error));
        }
        ApiError error = new ApiError(ErrorCode.UNAUTHORIZED.name(), "Invalid phone or password", List.of());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        ApiError error = new ApiError(ErrorCode.FORBIDDEN.name(), "Forbidden", List.of());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(error));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataConflict(DataIntegrityViolationException ex) {
        ApiError error = new ApiError(ErrorCode.CONFLICT.name(), "Data conflict", List.of(mostSpecificMessage(ex)));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(error));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String detail = ex.getSupportedHttpMethods() == null || ex.getSupportedHttpMethods().isEmpty()
            ? "Supported methods are not available"
            : "Supported methods: " + ex.getSupportedHttpMethods();
        ApiError error = new ApiError(
            ErrorCode.METHOD_NOT_ALLOWED.name(),
            "Request method is not supported for this endpoint",
            List.of(detail)
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error(error));
    }

    @ExceptionHandler({InvalidDataAccessResourceUsageException.class, SQLGrammarException.class})
    public ResponseEntity<ApiResponse<Void>> handleSqlGrammar(Exception ex) {
        ApiError error = new ApiError(
            ErrorCode.INTERNAL_ERROR.name(),
            "Database schema mismatch or invalid SQL",
            List.of(mostSpecificMessage(ex))
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException ex) {
        ApiError error = new ApiError(
            ErrorCode.INTERNAL_ERROR.name(),
            "Database access error",
            List.of(mostSpecificMessage(ex))
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiError error = new ApiError(ErrorCode.INTERNAL_ERROR.name(), "Unexpected error", List.of(mostSpecificMessage(ex)));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error));
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private String mostSpecificMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? throwable.getClass().getSimpleName() : message;
    }
}
