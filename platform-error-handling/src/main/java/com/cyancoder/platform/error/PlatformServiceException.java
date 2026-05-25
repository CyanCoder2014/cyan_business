package com.cyancoder.platform.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PlatformServiceException extends RuntimeException {
    private final PlatformErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final String englishMessage;
    private final String farsiMessage;
    private final Map<String, Object> details;

    public PlatformServiceException(
            PlatformErrorCode errorCode,
            HttpStatus httpStatus,
            String englishMessage,
            String farsiMessage,
            Map<String, Object> details,
            Throwable cause
    ) {
        super(englishMessage, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.englishMessage = englishMessage;
        this.farsiMessage = farsiMessage;
        this.details = details == null ? Map.of() : details;
    }

    public PlatformErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getEnglishMessage() {
        return englishMessage;
    }

    public String getFarsiMessage() {
        return farsiMessage;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
