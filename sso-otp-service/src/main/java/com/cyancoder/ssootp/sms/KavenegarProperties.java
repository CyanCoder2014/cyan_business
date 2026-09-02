package com.cyancoder.ssootp.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Kavenegar verify/lookup settings.
 *
 * <p>Templates are registered in the Kavenegar panel and carry their own text,
 * so a Farsi and an English message are two different templates rather than one
 * template with translated content. They are keyed by purpose and language:
 *
 * <pre>
 * otp.sms.kavenegar.templates.login.fa=cyan-login-fa
 * otp.sms.kavenegar.templates.login.en=cyan-login-en
 * otp.sms.kavenegar.templates.password-reset.fa=cyan-reset-fa
 * </pre>
 */
@ConfigurationProperties(prefix = "otp.sms.kavenegar")
public class KavenegarProperties {
    private String apiKey = "";
    /** Used when no purpose/language specific template is configured. */
    private String template = "";
    private String defaultLanguage = "fa";
    private Map<String, Map<String, String>> templates = new LinkedHashMap<>();

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
    public String getDefaultLanguage() { return defaultLanguage; }
    public void setDefaultLanguage(String defaultLanguage) { this.defaultLanguage = defaultLanguage; }
    public Map<String, Map<String, String>> getTemplates() { return templates; }
    public void setTemplates(Map<String, Map<String, String>> templates) { this.templates = templates; }

    /**
     * Resolves the template for a purpose and language, falling back to the
     * default language for that purpose and finally to the single legacy
     * template. Returns blank when nothing is configured, which the sender
     * treats as "not configured" rather than sending against a wrong template.
     */
    public String resolveTemplate(String purpose, String language) {
        Map<String, String> byLanguage = templates.get(normalize(purpose));
        if (byLanguage != null) {
            String exact = byLanguage.get(normalize(language));
            if (isSet(exact)) return exact;
            String fallback = byLanguage.get(normalize(defaultLanguage));
            if (isSet(fallback)) return fallback;
        }
        return template == null ? "" : template;
    }

    private boolean isSet(String value) { return value != null && !value.isBlank(); }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
