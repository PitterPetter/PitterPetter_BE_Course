package com.example.poi.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
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
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(new ErrorResponse("error", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        List<FieldErrorResponse> errors = new ArrayList<>();
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormat) {
            List<String> path = invalidFormat.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : String.valueOf(ref.getIndex()))
                    .toList();
            String field = String.join(".", path);
            errors.add(new FieldErrorResponse(field, invalidFormat.getOriginalMessage()));
        } else {
            errors.add(new FieldErrorResponse("request", "Malformed JSON request"));
        }
        return ResponseEntity.badRequest().body(new ErrorResponse("error", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        FieldErrorResponse error = new FieldErrorResponse("request", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("error", List.of(error)));
    }

    private FieldErrorResponse toFieldError(FieldError error) {
        return new FieldErrorResponse(error.getField(), error.getDefaultMessage());
    }
}
