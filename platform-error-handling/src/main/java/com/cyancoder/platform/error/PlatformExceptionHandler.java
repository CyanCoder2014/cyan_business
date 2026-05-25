package com.cyancoder.platform.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class PlatformExceptionHandler {
    private final PlatformErrorLocalizationService localizationService;

    public PlatformExceptionHandler(PlatformErrorLocalizationService localizationService) {
        this.localizationService = localizationService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PlatformErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ErrorLocale locale = localizationService.resolveLocale(request.getHeader("Accept-Language"));
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("validationErrors", ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage()
                ))
                .toList());
        PlatformErrorLocalizationService.LocalizedErrorDescriptor descriptor =
                new PlatformErrorLocalizationService.LocalizedErrorDescriptor(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        PlatformErrorCode.VALIDATION_ERROR.code(),
                        locale == ErrorLocale.FA ? "اعتبارسنجی درخواست ناموفق بود." : "Request validation failed.",
                        details
                );
        return build(descriptor, request);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<PlatformErrorResponse> handleThrowable(Throwable throwable, HttpServletRequest request) {
        return build(localizationService.resolve(throwable, localizationService.resolveLocale(request.getHeader("Accept-Language"))), request);
    }

    private ResponseEntity<PlatformErrorResponse> build(PlatformErrorLocalizationService.LocalizedErrorDescriptor descriptor, HttpServletRequest request) {
        PlatformErrorResponse body = new PlatformErrorResponse(
                Instant.now(),
                descriptor.status().value(),
                descriptor.status().getReasonPhrase(),
                descriptor.errorCode(),
                descriptor.message(),
                request.getRequestURI(),
                descriptor.details()
        );
        return ResponseEntity.status(descriptor.status()).body(body);
    }
}
