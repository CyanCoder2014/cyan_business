package com.cyancoder.ssocaptcha.service;

import com.cyancoder.sso.common.dto.CaptchaChallengeResponse;
import com.cyancoder.sso.common.dto.CaptchaVerifyResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CaptchaService {

    private final Map<String, StoredCaptcha> challenges = new ConcurrentHashMap<>();

    public CaptchaChallengeResponse createChallenge(String clientId) {
        int left = ThreadLocalRandom.current().nextInt(1, 10);
        int right = ThreadLocalRandom.current().nextInt(1, 10);
        String challengeId = UUID.randomUUID().toString();
        long expiresAt = Instant.now().plusSeconds(300).getEpochSecond();

        challenges.put(challengeId, new StoredCaptcha(String.valueOf(left + right), clientId, expiresAt));
        return new CaptchaChallengeResponse(challengeId, left + " + " + right + " = ?", expiresAt);
    }

    public CaptchaVerifyResponse verify(String challengeId, String answer, String clientId) {
        StoredCaptcha storedCaptcha = challenges.remove(challengeId);
        if (storedCaptcha == null) {
            return new CaptchaVerifyResponse(false, "Challenge not found");
        }
        if (storedCaptcha.expiresAtEpochSecond() < Instant.now().getEpochSecond()) {
            return new CaptchaVerifyResponse(false, "Challenge expired");
        }
        if (storedCaptcha.clientId() != null && clientId != null && !storedCaptcha.clientId().equals(clientId)) {
            return new CaptchaVerifyResponse(false, "Client mismatch");
        }
        if (!storedCaptcha.answer().equals(answer)) {
            return new CaptchaVerifyResponse(false, "Invalid captcha answer");
        }
        return new CaptchaVerifyResponse(true, "Captcha verified");
    }

    private record StoredCaptcha(String answer, String clientId, long expiresAtEpochSecond) {
    }
}
