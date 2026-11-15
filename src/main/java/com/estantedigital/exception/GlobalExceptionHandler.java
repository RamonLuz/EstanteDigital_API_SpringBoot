package com.estantedigital.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.estantedigital.dto.ApiExceptionDTO;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiExceptionDTO> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String field = ex.getName().toUpperCase();
        String message = ex.getRequiredType() != null && Number.class.isAssignableFrom(ex.getRequiredType())
                ? field + " must be numeric"
                : field + " has an invalid value";

        return ResponseEntity.badRequest().body(new ApiExceptionDTO(400, message, field));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiExceptionDTO> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .sorted((first, second) -> first.getField().compareTo(second.getField()))
                .toList();
        if (errors.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiExceptionDTO(400, "Request validation failed", "Body"));
        }

        String message = errors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ApiExceptionDTO(
                400,
                message,
                errors.get(0).getField()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiExceptionDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiExceptionDTO(409, "Resource conflicts with existing data", "Body"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiExceptionDTO> handleBodyMissing(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiExceptionDTO(400, "Request body is missing or malformed", "Body"));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiExceptionDTO> handleApiException(ApiException ex) {
        ApiExceptionDTO response = new ApiExceptionDTO(
                ex.getStatus(),
                ex.getMessage(),
                ex.getField());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(DomainRuleViolationException.class)
    public ResponseEntity<ApiExceptionDTO> handleDomainRuleViolation(DomainRuleViolationException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiExceptionDTO(400, ex.getMessage(), ex.getField()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiExceptionDTO> handleResponseStatus(ResponseStatusException ex) {
        int status = ex.getStatusCode().value();
        ApiExceptionDTO response = new ApiExceptionDTO(
                status,
                ex.getReason() == null ? "Request failed" : ex.getReason(),
                "Body");

        return ResponseEntity.status(status).body(response);
    }
}