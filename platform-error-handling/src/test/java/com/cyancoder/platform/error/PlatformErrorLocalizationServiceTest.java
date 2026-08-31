package com.cyancoder.platform.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformErrorLocalizationServiceTest {

    private final PlatformErrorLocalizationService service =
            new PlatformErrorLocalizationService(new ObjectMapper().registerModule(new JavaTimeModule()));

    @Test
    void defaultsToEnglish() {
        var descriptor = service.resolve(new IllegalArgumentException("bad input"), ErrorLocale.EN);

        assertEquals(HttpStatus.BAD_REQUEST, descriptor.status());
        assertEquals("ERR_VALIDATION", descriptor.errorCode());
        assertEquals("Request validation failed.", descriptor.message());
    }

    @Test
    void returnsFarsiWhenAcceptLanguageStartsWithFa() {
        var descriptor = service.resolve(new NoSuchElementException(), ErrorLocale.FA);

        assertEquals(HttpStatus.NOT_FOUND, descriptor.status());
        assertEquals("ERR_NOT_FOUND", descriptor.errorCode());
        assertEquals("منبع درخواستی پیدا نشد.", descriptor.message());
    }

    @Test
    void resolvesLocaleFromHeader() {
        assertEquals(ErrorLocale.FA, service.resolveLocale("fa-IR,fa;q=0.9,en;q=0.8"));
        assertEquals(ErrorLocale.EN, service.resolveLocale("en-US,en;q=0.9"));
        assertEquals(ErrorLocale.EN, service.resolveLocale(null));
    }

    @Test
    void preservesResponseStatusAndActionableReason() {
        var descriptor = service.resolve(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP code is required"),
                ErrorLocale.EN
        );

        assertEquals(HttpStatus.BAD_REQUEST, descriptor.status());
        assertEquals("ERR_VALIDATION", descriptor.errorCode());
        assertEquals("OTP code is required", descriptor.message());
        assertEquals("OTP code is required", descriptor.details().get("reason"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void relaysDownstreamValidationErrorsInsteadOfCollapsingToInternalError() {
        String downstreamBody = """
                {
                  "timestamp": "2026-08-21T12:18:05.102613305Z",
                  "status": 400,
                  "error": "Bad Request",
                  "errorCode": "ERR_VALIDATION",
                  "message": "Request validation failed.",
                  "path": "/internal/entities/submit/sfsdf",
                  "details": {"validationErrors": [{"field": "fff[0].d", "message": "xdfsdfs"}]},
                  "fieldErrors": [{"field": "fff[0].d", "code": "INVALID", "message": "xdfsdfs"}],
                  "correlationId": "1e9cc193-bc27-4cc2-a57d-cd3dddd2271b",
                  "retryable": false
                }
                """;
        HttpClientErrorException downstream = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                downstreamBody.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );

        var descriptor = service.resolve(downstream, ErrorLocale.EN);

        assertEquals(HttpStatus.BAD_REQUEST, descriptor.status());
        assertEquals("ERR_VALIDATION", descriptor.errorCode());
        assertEquals("Request validation failed.", descriptor.message());
        var validationErrors = (java.util.List<java.util.Map<String, Object>>) descriptor.details().get("validationErrors");
        assertEquals(1, validationErrors.size());
        assertEquals("fff[0].d", validationErrors.get(0).get("field"));
        assertEquals("xdfsdfs", validationErrors.get(0).get("message"));
    }

    @Test
    void fallsBackToDownstreamStatusWhenBodyIsNotParseable() {
        HttpClientErrorException downstream = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                "not json".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );

        var descriptor = service.resolve(downstream, ErrorLocale.EN);

        assertEquals(HttpStatus.BAD_REQUEST, descriptor.status());
        assertEquals("ERR_DOWNSTREAM_SERVICE", descriptor.errorCode());
        assertTrue(descriptor.details().get("reason").toString().contains("400"));
    }

    @Test
    void reportsMissingRequestParameterAsClientErrorRatherThanInternalFailure() {
        var descriptor = service.resolve(
                new MissingServletRequestParameterException("captchaChallengeId", "String"),
                ErrorLocale.EN
        );

        assertEquals(HttpStatus.BAD_REQUEST, descriptor.status());
        assertEquals("ERR_VALIDATION", descriptor.errorCode());
        assertEquals("A required request parameter is missing or invalid.", descriptor.message());
        assertTrue(descriptor.details().get("reason").toString().contains("captchaChallengeId"));
    }
}
