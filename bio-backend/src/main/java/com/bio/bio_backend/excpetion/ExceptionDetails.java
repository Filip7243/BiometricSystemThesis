package com.bio.bio_backend.excpetion;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;

public record ExceptionDetails(String message, HttpStatus status, ZonedDateTime timestamp) {

    public static ExceptionDetails createDetails(String e, HttpStatus status) {
        return new ExceptionDetails(e, status, now(of("Z")));
    }
}