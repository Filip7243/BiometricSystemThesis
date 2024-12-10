package com.bio.bio_backend.excpetion;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.bio.bio_backend.excpetion.ExceptionDetails.createDetails;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleRoleNotFoundException(EntityNotFoundException e) {
        ExceptionDetails details = createDetails(e.getMessage(), NOT_FOUND);

        return new ResponseEntity<>(details, details.status());
    }
}
