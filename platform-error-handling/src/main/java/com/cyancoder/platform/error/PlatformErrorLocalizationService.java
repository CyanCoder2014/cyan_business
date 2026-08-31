package com.cyancoder.platform.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

public class PlatformErrorLocalizationService {

    private final ObjectMapper objectMapper;

    public PlatformErrorLocalizationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LocalizedErrorDescriptor resolve(Throwable throwable, ErrorLocale locale) {
        if (throwable instanceof PlatformServiceException ex) {
            return new LocalizedErrorDescriptor(
                    ex.getHttpStatus(),
                    ex.getErrorCode().code(),
                    locale == ErrorLocale.FA ? ex.getFarsiMessage() : ex.getEnglishMessage(),
                    ex.getDetails()
            );
        }
        if (throwable instanceof RestClientResponseException ex) {
            PlatformErrorResponse downstream = tryParseDownstreamError(ex);
            if (downstream != null) {
                Map<String, Object> details = new LinkedHashMap<>(
                        downstream.details() == null ? Map.of() : downstream.details()
                );
                if (downstream.fieldErrors() != null && !downstream.fieldErrors().isEmpty()) {
                    details.put("validationErrors", downstream.fieldErrors().stream()
                            .map(fieldError -> Map.of("field", fieldError.field(), "message", fieldError.message()))
                            .toList());
                }
                HttpStatus status = HttpStatus.resolve(downstream.status());
                return new LocalizedErrorDescriptor(
                        status == null ? HttpStatus.BAD_GATEWAY : status,
                        downstream.errorCode() == null ? PlatformErrorCode.DOWNSTREAM_SERVICE_ERROR.code() : downstream.errorCode(),
                        downstream.message() == null || downstream.message().isBlank() ? "A downstream service call failed." : downstream.message(),
                        details
                );
            }
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            return descriptor(
                    status == null ? HttpStatus.BAD_GATEWAY : status,
                    PlatformErrorCode.DOWNSTREAM_SERVICE_ERROR,
                    "A downstream service call failed.",
                    "فراخوانی سرویس داخلی با خطا مواجه شد.",
                    locale,
                    Map.of("reason", safeMessage(ex))
            );
        }
        if (throwable instanceof ResponseStatusException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            String reason = ex.getReason();
            if (reason == null || reason.isBlank()) {
                reason = status.getReasonPhrase();
            }
            PlatformErrorCode code = switch (status) {
                case UNAUTHORIZED, FORBIDDEN -> PlatformErrorCode.ACCESS_DENIED;
                case NOT_FOUND -> PlatformErrorCode.RESOURCE_NOT_FOUND;
                case CONFLICT, PRECONDITION_FAILED -> PlatformErrorCode.ILLEGAL_STATE;
                case BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT -> PlatformErrorCode.DOWNSTREAM_SERVICE_ERROR;
                default -> status.is4xxClientError()
                        ? PlatformErrorCode.VALIDATION_ERROR
                        : PlatformErrorCode.INTERNAL_ERROR;
            };
            return new LocalizedErrorDescriptor(
                    status,
                    code.code(),
                    reason,
                    Map.of("reason", reason)
            );
        }
        if (throwable instanceof IllegalArgumentException ex) {
            return descriptor(
                    HttpStatus.BAD_REQUEST,
                    PlatformErrorCode.VALIDATION_ERROR,
                    "Request validation failed.",
                    "اعتبارسنجی درخواست ناموفق بود.",
                    locale,
                    Map.of("reason", safeMessage(ex))
            );
        }
        // Spring raises this when no controller mapping matched and the request fell through to static
        // resource handling. Matched by name so this module keeps no hard dependency on spring-webmvc's
        // resource package. Without it an unknown route reports as a retryable 500 "unexpected internal
        // error", which reads as a broken endpoint rather than a missing one.
        if (simpleName(throwable).equals("NoResourceFoundException") || simpleName(throwable).equals("NoHandlerFoundException")) {
            return descriptor(
                    HttpStatus.NOT_FOUND,
                    PlatformErrorCode.RESOURCE_NOT_FOUND,
                    "No endpoint matches this request path.",
                    "مسیر درخواست‌شده وجود ندارد.",
                    locale,
                    Map.of()
            );
        }
        if (throwable instanceof NoSuchElementException) {
            return descriptor(
                    HttpStatus.NOT_FOUND,
                    PlatformErrorCode.RESOURCE_NOT_FOUND,
                    "Requested resource was not found.",
                    "منبع درخواستی پیدا نشد.",
                    locale,
                    Map.of()
            );
        }
        if (throwable instanceof HttpMessageNotReadableException || throwable instanceof MethodArgumentTypeMismatchException) {
            return descriptor(
                    HttpStatus.BAD_REQUEST,
                    PlatformErrorCode.MALFORMED_REQUEST,
                    "Request body is malformed or incompatible.",
                    "بدنه درخواست نامعتبر یا ناسازگار است.",
                    locale,
                    Map.of("reason", safeMessage(throwable))
            );
        }
        // Missing or unbindable query parameters, headers, and cookies. Without
        // this the caller's own mistake reports as a retryable 500, which reads
        // as a broken endpoint and invites pointless retries.
        if (throwable instanceof ServletRequestBindingException) {
            return descriptor(
                    HttpStatus.BAD_REQUEST,
                    PlatformErrorCode.VALIDATION_ERROR,
                    "A required request parameter is missing or invalid.",
                    "یکی از پارامترهای الزامی درخواست وجود ندارد یا نامعتبر است.",
                    locale,
                    Map.of("reason", safeMessage(throwable))
            );
        }
        if (throwable instanceof AccessDeniedException) {
            return descriptor(
                    HttpStatus.FORBIDDEN,
                    PlatformErrorCode.ACCESS_DENIED,
                    "Access is denied.",
                    "دسترسی مجاز نیست.",
                    locale,
                    Map.of()
            );
        }
        if (simpleName(throwable).equals("CommandExecutionException")) {
            return descriptor(
                    HttpStatus.CONFLICT,
                    PlatformErrorCode.COMMAND_EXECUTION_ERROR,
                    "Command execution failed.",
                    "اجرای دستور با خطا مواجه شد.",
                    locale,
                    Map.of("reason", safeMessage(throwable))
            );
        }
        if (throwable instanceof IllegalStateException) {
            return descriptor(
                    HttpStatus.CONFLICT,
                    PlatformErrorCode.ILLEGAL_STATE,
                    "The requested operation is not valid in the current state.",
                    "عملیات در وضعیت فعلی معتبر نیست.",
                    locale,
                    Map.of("reason", safeMessage(throwable))
            );
        }
        return descriptor(
                HttpStatus.INTERNAL_SERVER_ERROR,
                PlatformErrorCode.INTERNAL_ERROR,
                "An unexpected internal error occurred.",
                "یک خطای داخلی غیرمنتظره رخ داد.",
                locale,
                Map.of("reason", safeMessage(throwable))
        );
    }

    public ErrorLocale resolveLocale(String acceptLanguageHeader) {
        if (acceptLanguageHeader == null || acceptLanguageHeader.isBlank()) {
            return ErrorLocale.EN;
        }
        String normalized = acceptLanguageHeader.toLowerCase(Locale.ROOT);
        return normalized.startsWith("fa") ? ErrorLocale.FA : ErrorLocale.EN;
    }

    private LocalizedErrorDescriptor descriptor(
            HttpStatus status,
            PlatformErrorCode code,
            String englishMessage,
            String farsiMessage,
            ErrorLocale locale,
            Map<String, Object> details
    ) {
        return new LocalizedErrorDescriptor(
                status,
                code.code(),
                locale == ErrorLocale.FA ? farsiMessage : englishMessage,
                sanitizeDetails(details)
        );
    }

    private Map<String, Object> sanitizeDetails(Map<String, Object> details) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        details.forEach((key, value) -> {
            if (value != null) {
                sanitized.put(key, value);
            }
        });
        return sanitized;
    }

    private PlatformErrorResponse tryParseDownstreamError(RestClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, PlatformErrorResponse.class);
        } catch (Exception parseFailure) {
            return null;
        }
    }

    private String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message == null || message.isBlank() ? throwable.getClass().getSimpleName() : message;
    }

    private String simpleName(Throwable throwable) {
        return throwable.getClass().getSimpleName();
    }

    public record LocalizedErrorDescriptor(
            HttpStatus status,
            String errorCode,
            String message,
            Map<String, Object> details
    ) {
    }
}
