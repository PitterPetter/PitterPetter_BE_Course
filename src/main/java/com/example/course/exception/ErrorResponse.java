package com.example.course.exception;

import java.util.List;

public class ErrorResponse {

    private final String status;
    private final List<FieldErrorResponse> errors;

    public ErrorResponse(String status, List<FieldErrorResponse> errors) {
        this.status = status;
        this.errors = errors;
    }

    public String getStatus() {
        return status;
    }

    public List<FieldErrorResponse> getErrors() {
        return errors;
    }
}
