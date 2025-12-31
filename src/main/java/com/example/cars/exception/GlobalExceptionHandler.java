package com.example.cars.exception;

import com.example.cars.model.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {
        
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            status.getReasonPhrase(),
            ex.getReason() != null ? ex.getReason() : status.getReasonPhrase(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        
        String message = "Invalid request body format";
        
        // Check for InvalidFormatException in the cause chain
        Throwable cause = ex.getCause();
        InvalidFormatException ife = null;
        while (cause != null) {
            if (cause instanceof InvalidFormatException) {
                ife = (InvalidFormatException) cause;
                break;
            }
            cause = cause.getCause();
        }
        
        if (ife != null) {
            String fieldName = "field";
            if (ife.getPath() != null && !ife.getPath().isEmpty()) {
                var lastPath = ife.getPath().get(ife.getPath().size() - 1);
                if (lastPath != null && lastPath.getFieldName() != null) {
                    fieldName = lastPath.getFieldName();
                }
            }
            String value = ife.getValue() != null ? ife.getValue().toString() : "null";
            String type = ife.getTargetType() != null 
                ? ife.getTargetType().getSimpleName() : "number";
            
            message = String.format("Invalid value '%s' for field '%s'. Expected %s.", 
                value, fieldName, type);
        } else if (ex.getMessage() != null) {
            // Fallback: extract info from message
            String msg = ex.getMessage();
            // Look for pattern: "String \"199s\"" or "not a valid"
            if (msg.contains("String \"")) {
                int start = msg.indexOf("String \"") + 8;
                int end = msg.indexOf("\"", start);
                if (end > start) {
                    String value = msg.substring(start, end);
                    message = String.format("Invalid value '%s'. Expected a valid number.", value);
                }
            } else if (msg.contains("not a valid")) {
                message = "Invalid value format. Expected a valid number.";
            }
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            message,
            request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
            request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

