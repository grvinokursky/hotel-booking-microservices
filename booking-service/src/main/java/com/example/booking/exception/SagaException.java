package com.example.booking.exception;

public class SagaException extends RuntimeException {
    public SagaException(String msg, Throwable cause) { super(msg, cause); }
    public SagaException(String msg) { super(msg); }
}
