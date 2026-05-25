package com.cyancoder.aiorchestrator.exception;

import com.cyancoder.platform.error.PlatformErrorCode;
import com.cyancoder.platform.error.PlatformServiceException;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class DownstreamServiceException extends PlatformServiceException {
    private final String serviceKey;
    private final String path;
    private final Integer downstreamStatus;
    private final String responseBody;

    public DownstreamServiceException(String message, String serviceKey, String path, Integer downstreamStatus, String responseBody, Throwable cause) {
        super(
                PlatformErrorCode.DOWNSTREAM_SERVICE_ERROR,
                HttpStatus.SERVICE_UNAVAILABLE,
                "Downstream service call failed.",
                "فراخوانی سرویس پایین‌دستی ناموفق بود.",
                details(serviceKey, path, downstreamStatus, responseBody, message),
                cause
        );
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

    private static Map<String, Object> details(String serviceKey, String path, Integer downstreamStatus, String responseBody, String message) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("serviceKey", serviceKey);
        details.put("path", path);
        details.put("downstreamStatus", downstreamStatus);
        details.put("downstreamBody", responseBody);
        details.put("reason", message);
        return details;
    }
}
