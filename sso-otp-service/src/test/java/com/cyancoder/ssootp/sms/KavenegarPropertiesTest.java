package com.cyancoder.ssootp.sms;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KavenegarPropertiesTest {

    private KavenegarProperties properties() {
        KavenegarProperties properties = new KavenegarProperties();
        properties.setApiKey("key");
        properties.setDefaultLanguage("fa");
        properties.setTemplates(Map.of(
                "login", Map.of("fa", "cyan-login-fa", "en", "cyan-login-en"),
                "password-reset", Map.of("fa", "cyan-reset-fa")
        ));
        return properties;
    }

    @Test
    void picksTheTemplateForThePurposeAndLanguage() {
        assertEquals("cyan-login-fa", properties().resolveTemplate("LOGIN", "fa"));
        assertEquals("cyan-login-en", properties().resolveTemplate("LOGIN", "en"));
        assertEquals("cyan-reset-fa", properties().resolveTemplate("PASSWORD_RESET", "fa"));
    }

    @Test
    void fallsBackToTheDefaultLanguageRatherThanSendingNothing() {
        // No English reset template is registered yet.
        assertEquals("cyan-reset-fa", properties().resolveTemplate("PASSWORD_RESET", "en"));
    }

    @Test
    void underscoredPurposeAndMixedCaseLanguageStillMatch() {
        assertEquals("cyan-reset-fa", properties().resolveTemplate("password_reset", "FA"));
    }

    @Test
    void unknownPurposeFallsBackToTheLegacySingleTemplate() {
        KavenegarProperties properties = properties();
        properties.setTemplate("legacy-template");
        assertEquals("legacy-template", properties.resolveTemplate("SOMETHING_ELSE", "fa"));
    }

    @Test
    void returnsBlankWhenNothingIsConfiguredSoTheSenderRefusesToSend() {
        KavenegarProperties properties = new KavenegarProperties();
        assertTrue(properties.resolveTemplate("LOGIN", "fa").isBlank());
    }
}
