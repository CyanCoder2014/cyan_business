package com.cyancoder.batchworker.service;

public class SkippableItemException extends RuntimeException {
    public SkippableItemException(String message) { super(message); }
}
