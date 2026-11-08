package com.example.booking.exception;

import com.example.booking.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> nf(NotFoundException e, HttpServletRequest r) { return build(HttpStatus.NOT_FOUND, e.getMessage(), r); }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> cf(ConflictException e, HttpServletRequest r) { return build(HttpStatus.CONFLICT, e.getMessage(), r); }

    @ExceptionHandler(SagaException.class)
    public ResponseEntity<ErrorResponse> sg(SagaException e, HttpServletRequest r) { return build(HttpStatus.BAD_GATEWAY, e.getMessage(), r); }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> ad(AccessDeniedException e, HttpServletRequest r) { return build(HttpStatus.FORBIDDEN, e.getMessage(), r); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> val(MethodArgumentNotValidException e, HttpServletRequest r) {
        String msg = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, msg, r);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> gen(Exception e, HttpServletRequest r) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), r);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus s, String m, HttpServletRequest r) {
        return ResponseEntity.status(s).body(new ErrorResponse(s.value(), s.getReasonPhrase(), m, r.getRequestURI(), Instant.now()));
    }
}
