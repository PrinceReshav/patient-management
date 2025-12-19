package com.pm.patientservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Lets us handle cross-cutting concerns outside controller and services
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(
            GlobalExceptionHandler.class);

    // This annotation tells Spring that this method should handle exceptions
// of type MethodArgumentNotValidException whenever they occur in the controller layer.
// For example, when @Valid fails on a request body, this exception is thrown.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {

        // Create a Map to hold the field names and their corresponding validation error messages.
        // Example: {"email": "must be a valid email", "name": "must not be blank"}
        Map<String, String> errors = new HashMap<>();

        // The exception contains a BindingResult object which stores all validation errors
        // captured during @Valid annotation processing.
        // getBindingResult() → returns the result of the validation
        // getFieldErrors() → returns a list of field-level validation errors (each one is a FieldError object)
        ex.getBindingResult().getFieldErrors().forEach(
                error ->
                        // For each field error, we extract:
                        // 1. The field name that failed validation (error.getField())
                        // 2. The default validation message (error.getDefaultMessage())
                        // And put it inside our 'errors' map.
                        errors.put(error.getField(), error.getDefaultMessage())
        );

        // Finally, we return a ResponseEntity with HTTP status 400 (Bad Request)
        // and the body containing the 'errors' map.
        // ResponseEntity allows customizing both status and body in a structured way.
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {


        log.warn("Email address already exists {} ", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Email already exists! ");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePatientNotFoundException(
            PatientNotFoundException ex){
        log.warn("Patient not found {} ", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Patient not found! ");
        return ResponseEntity.badRequest().body(errors);
    }
}
