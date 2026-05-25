package com.cyancoder.platform.error;

public enum PlatformErrorCode {
    VALIDATION_ERROR("ERR_VALIDATION"),
    RESOURCE_NOT_FOUND("ERR_NOT_FOUND"),
    MALFORMED_REQUEST("ERR_MALFORMED_REQUEST"),
    ACCESS_DENIED("ERR_ACCESS_DENIED"),
    DOWNSTREAM_SERVICE_ERROR("ERR_DOWNSTREAM_SERVICE"),
    LLM_PROVIDER_ERROR("ERR_LLM_PROVIDER"),
    COMMAND_EXECUTION_ERROR("ERR_COMMAND_EXECUTION"),
    ILLEGAL_STATE("ERR_ILLEGAL_STATE"),
    INTERNAL_ERROR("ERR_INTERNAL");

    private final String code;

    PlatformErrorCode(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
