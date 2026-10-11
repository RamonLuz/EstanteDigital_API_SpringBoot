package com.estantedigital.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.estantedigital.dto.ApiExceptionDTO;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiExceptionDTO> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiExceptionDTO response = new ApiExceptionDTO(
                400,
                "ID must be numeric",
                "ID");

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({ DataIntegrityViolationException.class, MethodArgumentNotValidException.class })
    public ResponseEntity<ApiExceptionDTO> handleDataIntegrity(Exception ex) {
        ApiExceptionDTO response = new ApiExceptionDTO(
                400,
                "Required fields were not provided",
                "Body");

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiExceptionDTO> handleBodyMissing(HttpMessageNotReadableException ex) {
        ApiExceptionDTO response = new ApiExceptionDTO(
                400,
                "Request body is required",
                "Body");

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiExceptionDTO> handleApiException(ApiException ex) {
        ApiExceptionDTO response = new ApiExceptionDTO(
                ex.getStatus(),
                ex.getMessage(),
                ex.getField());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}