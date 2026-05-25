package com.cyancoder.aiorchestrator.exception;

public class DownstreamServiceException extends RuntimeException {
    private final String serviceKey;
    private final String path;
    private final Integer downstreamStatus;
    private final String responseBody;

    public DownstreamServiceException(String message, String serviceKey, String path, Integer downstreamStatus, String responseBody, Throwable cause) {
        super(message, cause);
        this.serviceKey = serviceKey;
        this.path = path;
        this.downstreamStatus = downstreamStatus;
        this.responseBody = responseBody;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public String getPath() {
        return path;
    }

    public Integer getDownstreamStatus() {
        return downstreamStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
