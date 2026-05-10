package com.gigu.accessprofile.interfaces.rest;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }

    @ExceptionHandler({SecurityException.class, AccessDeniedException.class})
    ResponseEntity<Map<String, Object>> forbidden(Exception e) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "forbidden")); }
}
