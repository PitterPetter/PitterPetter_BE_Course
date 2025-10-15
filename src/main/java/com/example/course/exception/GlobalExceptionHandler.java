package com.example.course.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("[GlobalExceptionHandler] 요청 검증 실패 fieldErrors={}", ex.getBindingResult().getFieldErrors().size());
        List<FieldErrorResponse> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(new ErrorResponse("error", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("[GlobalExceptionHandler] 메시지 변환 실패 message={}", ex.getMessage());
        List<FieldErrorResponse> errors = new ArrayList<>();
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormat) {
            List<String> path = invalidFormat.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : String.valueOf(ref.getIndex()))
                    .toList();
            errors.add(new FieldErrorResponse(String.join(".", path), invalidFormat.getOriginalMessage()));
        } else {
            errors.add(new FieldErrorResponse("request", "Malformed JSON request"));
        }
        return ResponseEntity.badRequest().body(new ErrorResponse("error", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("[GlobalExceptionHandler] 잘못된 인자 예외 message={}", ex.getMessage());
        FieldErrorResponse error = new FieldErrorResponse("request", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("error", List.of(error)));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("[GlobalExceptionHandler] 엔티티 미발견 message={}", ex.getMessage());
        FieldErrorResponse error = new FieldErrorResponse("resource", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("error", List.of(error)));
    }

    private FieldErrorResponse toFieldError(FieldError error) {
        return new FieldErrorResponse(error.getField(), error.getDefaultMessage());
    }
}
