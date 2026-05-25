package com.cyancoder.bpm.service;

import com.cyancoder.bpm.config.DynamicFlowCallbackProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class AsyncCallbackSecurityService {
    private final DynamicFlowCallbackProperties properties;

    public AsyncCallbackSecurityService(DynamicFlowCallbackProperties properties) {
        this.properties = properties;
    }

    public void validate(String timestamp, String signature, byte[] canonicalBody) {
        if (!properties.isEnabled()) {
            return;
        }
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            throw new IllegalArgumentException("callback secret not configured");
        }
        if (timestamp == null || timestamp.isBlank() || signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("callback signature missing");
        }
        long ts = Long.parseLong(timestamp);
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - ts) > properties.getMaxSkewSeconds()) {
            throw new IllegalArgumentException("callback signature expired");
        }
        String expected = sign(timestamp, canonicalBody);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("callback signature invalid");
        }
    }

    public String sign(String timestamp, byte[] canonicalBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update(timestamp.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) '.');
            mac.update(canonicalBody);
            return HexFormat.of().formatHex(mac.doFinal());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign callback payload", ex);
        }
    }
}

