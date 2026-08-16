package com.cyancoder.platform.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlatformErrorLocalizationServiceTest {

    private final PlatformErrorLocalizationService service = new PlatformErrorLocalizationService();

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
}
