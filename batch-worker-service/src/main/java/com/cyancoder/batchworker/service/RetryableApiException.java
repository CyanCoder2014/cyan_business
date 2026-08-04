package com.cyancoder.batchworker.service;

public class RetryableApiException extends RuntimeException {
    public RetryableApiException(String message) { super(message); }
    public RetryableApiException(String message, Throwable cause) { super(message, cause); }
}
