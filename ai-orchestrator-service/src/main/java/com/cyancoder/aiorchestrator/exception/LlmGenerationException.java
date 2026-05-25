package com.cyancoder.aiorchestrator.exception;

import com.cyancoder.aiorchestrator.config.AiProvider;
import com.cyancoder.platform.error.PlatformErrorCode;
import com.cyancoder.platform.error.PlatformServiceException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class LlmGenerationException extends PlatformServiceException {
    private final Map<AiProvider, String> providerFailures;

    public LlmGenerationException(String message, Map<AiProvider, String> providerFailures) {
        super(
                PlatformErrorCode.LLM_PROVIDER_ERROR,
                HttpStatus.SERVICE_UNAVAILABLE,
                "No available LLM provider produced a valid response.",
                "هیچ ارائه‌دهنده مدل زبانیِ در دسترسی پاسخ معتبر تولید نکرد.",
                Map.of(
                        "providerFailures", providerFailures,
                        "reason", message
                ),
                null
        );
        this.providerFailures = providerFailures;
    }

    public Map<AiProvider, String> getProviderFailures() {
        return providerFailures;
    }
}
